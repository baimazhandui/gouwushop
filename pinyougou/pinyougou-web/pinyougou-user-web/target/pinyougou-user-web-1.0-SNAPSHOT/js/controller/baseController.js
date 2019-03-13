app.controller("baseController", function ($scope, $filter, baseService) {
    // 定义获取登录用户名方法
    $scope.showName = function () {
        // 定义重定向 URL
        $scope.redirectUrl = window.encodeURIComponent(location.href);
        baseService.sendGet("/user/showName")
            .then(function (response) {
                $scope.loginName = response.data.loginName;
            })
    };

    $scope.findUser = function () {
        baseService.sendGet("/user/findUser")
            .then(function (response) {
                $scope.user = response.data.user;
                var phone = $scope.user.phone;
                var showPhone = phone.substr(0,3) + "****"  + phone.substr(7);
                $scope.phone = showPhone;
                $scope.user.birthday = $filter('date')($scope.user.birthday, "yyyy-MM-dd");
                if ($scope.user.sex == "0") {
                    $scope.user.sex = "1";
                }
                if ($scope.user.area) {
                    $scope.user.area = JSON.parse($scope.user.area);
                }

            })
    };

    /** 定义分页配置信息对象 */
    $scope.paginationConf = {
        currentPage : 1, // 当前页码
        totalItems : 0, // 总记录数
        itemsPerPage : 10, // 每页显示的记录数
        perPageOptions : [10,15,20], // 页码下拉列表
        onChange : function() { // 改变事件
            $scope.reload();
        }
    };
    /** 当下拉列表页码发生改变，重新加载数据 */
    $scope.reload = function(){
        $scope.search($scope.paginationConf.currentPage,
            $scope.paginationConf.itemsPerPage);
    };
});