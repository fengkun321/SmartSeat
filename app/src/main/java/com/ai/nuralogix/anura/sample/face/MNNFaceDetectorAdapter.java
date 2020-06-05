package com.ai.nuralogix.anura.sample.face;

import android.content.Context;
import android.support.annotation.NonNull;

import com.alibaba.android.mnnkit.actor.FaceDetector;
import com.alibaba.android.mnnkit.entity.FaceDetectionReport;
import com.alibaba.android.mnnkit.entity.MNNCVImageFormat;
import com.alibaba.android.mnnkit.entity.MNNFlipType;
import com.alibaba.android.mnnkit.intf.InstanceCreatedListener;
import com.smartCarSeatProject.activity.BaseActivity;

import org.opencv.core.Point3;
import org.opencv.core.Rect;

import java.util.HashMap;

import ai.nuralogix.anurasdk.core.AnuraVideoFrame;
import ai.nuralogix.anurasdk.core.VideoFormat;
import ai.nuralogix.anurasdk.error.AnuraError;
import ai.nuralogix.anurasdk.face.FaceTrackerAdapter;
import ai.nuralogix.anurasdk.face.FaceTrackerAdapterListener;
import ai.nuralogix.anurasdk.utils.AnuLogUtil;
import ai.nuralogix.dfx.Face;
import ai.nuralogix.dfx.PosePoint;

/**
 * Created by zhandalin on 2020-03-11 11:07.
 * description:
 */
public class MNNFaceDetectorAdapter implements FaceTrackerAdapter {
    private static final String TAG = "MNNFaceDetectorAdapter";
    private final static int MAX_RESULT = 3;
    private FaceDetector mFaceDetector = null;
    private FaceTrackerAdapterListener faceTrackerAdapterListener = null;
    private HashMap<String, Face> faceHashMap = new HashMap<>();
    private long logIndex = 0;

    private BaseActivity.CheckPersionHaveListener checkPersionHaveListener = null;

    public MNNFaceDetectorAdapter(Context context) {
        if (null == context) {
            return;
        }
        FaceDetector.FaceDetectorCreateConfig createConfig = new FaceDetector.FaceDetectorCreateConfig();
        createConfig.mode = FaceDetector.FaceDetectMode.MOBILE_DETECT_MODE_VIDEO;
        FaceDetector.createInstanceAsync(context, createConfig, new InstanceCreatedListener<FaceDetector>() {
            @Override
            public void onSucceeded(FaceDetector faceDetector) {
                mFaceDetector = faceDetector;
                AnuLogUtil.d(TAG, "create createInstanceAsync onSucceeded");
            }

            @Override
            public void onFailed(int i, Error error) {
                AnuLogUtil.e(TAG, "create face detetector failed: " + error);
            }
        });
    }

    @Override
    public AnuraError open(@NonNull FaceTrackerAdapterListener faceTrackerAdapterListener) {
        this.faceTrackerAdapterListener = faceTrackerAdapterListener;
        return AnuraError.OK;
    }

    @Override
    public void close() {
        if (mFaceDetector != null) {
            mFaceDetector.release();
            mFaceDetector = null;
        }
    }

    public void setCheckPersionHaveListener(BaseActivity.CheckPersionHaveListener checkPersionHaveListener) {
        this.checkPersionHaveListener = checkPersionHaveListener;
    }

    @NonNull
    @Override
    public AnuraError track(@NonNull AnuraVideoFrame anuraVideoFrame, @NonNull String[] attributes) {
        if (null == anuraVideoFrame.getCvImage() || null == anuraVideoFrame.getPixelData()
                || null == faceTrackerAdapterListener || null == mFaceDetector) {
            return AnuraError.ERROR;
        }
        long start = System.currentTimeMillis();
        VideoFormat videoFormat = anuraVideoFrame.getVideoFormat();
        FaceDetectionReport[] results = mFaceDetector.inference(anuraVideoFrame.getPixelData(), videoFormat.getWidth(), videoFormat.getHeight(), MNNCVImageFormat.BGRA, 0, 0, 0, MNNFlipType.FLIP_NONE);
        if (logIndex++ % 30 == 0) {
            AnuLogUtil.d(TAG, "FaceDetector timeCost=" + (System.currentTimeMillis() - start));
        }

        if (null == results || results.length <= 0) {
            if (checkPersionHaveListener != null)
                checkPersionHaveListener.checkResult(false);
            faceHashMap.clear();
            faceTrackerAdapterListener.onFacePointsTracked(faceHashMap);
            faceTrackerAdapterListener.onFaceDetected(0);
            return AnuraError.OK;
        }

        if (checkPersionHaveListener != null)
            checkPersionHaveListener.checkResult(true);
        faceTrackerAdapterListener.onFaceDetected(Math.min(results.length, MAX_RESULT));
        Face faceObj;
        for (int i = 0; i < results.length && i < MAX_RESULT; i++) {
            if (faceHashMap.containsKey((i + 1) + "")) {
                faceObj = faceHashMap.get((i + 1) + "");
            } else {
                faceObj = new Face();
                faceObj.faceRect = new Rect();
                faceObj.posePoints = new HashMap<>();
                faceHashMap.put((i + 1) + "", faceObj);
            }
            faceObj.id = (i + 1) + "";
            faceObj.detected = true;
            faceObj.poseValid = true;
            faceObj.faceRect.x = results[i].rect.left;
            faceObj.faceRect.y = results[i].rect.top;
            faceObj.faceRect.width = results[i].rect.width();
            faceObj.faceRect.height = results[i].rect.height();

            HashMap<String, PosePoint> posePointMap = faceObj.posePoints;
            PosePoint posePoint;
            FaceDetectionReport faceDetectionReport = results[i];
            int size = faceDetectionReport.keyPoints.length;
            for (int j = 0; j < size; j = j + 2) {
                if (!FacePointMapUtil.FACE_POINT_MAP_MNNKIT.containsKey(j / 2)) {
                    continue;
                }
                String key = FacePointMapUtil.FACE_POINT_MAP_MNNKIT.get(j / 2);
                if (posePointMap.containsKey(key)) {
                    posePoint = posePointMap.get(key);
                } else {
                    posePoint = new PosePoint();
                }
                posePoint.quality = 1;
                posePoint.valid = true;
                if (null == posePoint.point) {
                    posePoint.point = new Point3();
                }
                posePoint.point.x = faceDetectionReport.keyPoints[j];
                posePoint.point.y = faceDetectionReport.keyPoints[j + 1];
                posePoint.point.z = 0;
                posePointMap.put(key, posePoint);
            }
        }
        faceTrackerAdapterListener.onFacePointsTracked(faceHashMap);

        return AnuraError.OK;
    }

    @Override
    public void stopTrack() {

    }

    @Override
    public void setTrackingRegion(int x, int y, int width, int height) {

    }

}
