/**URL配置管理*/
const URLManager = {
    /**服务器地址*/
    serverConnUrl: "https://www.derucci-smart.com/",//智慧慕思服务接口根地址
    // serverConnUrl: "http://test.derucci-smart.com/",//测试环境服务接口根地址
    /**服务器端接口*/
    serverAPI : {
        uploadMatchResultUrl : "t10/api/pressureMats/device/uploadMatchResult",//上传匹配结果接口
        updateCheckUrl : "t10/api/pressureMats/device/updateCheck"//检查更新接口
    },
    otherUrl:{
        trayAboutUs : "http://www.derucci.com"
    },
    /**界面的href管理*/
    pageHref : {

    }
};

/**是否开发模式*/
const IsDevMode = false;

// module.exports.URLManager = URLManager;
// module.exports.IsDevMode = IsDevMode;