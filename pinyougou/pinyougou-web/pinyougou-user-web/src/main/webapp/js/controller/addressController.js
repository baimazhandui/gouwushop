/** 定义控制器层 */
app.controller('addressController', function($scope, $controller, baseService) {

    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});

    $scope.findAddressByUser = function () {
        baseService.sendGet("/user/findAddressByUser")
            .then(function (response) {
                $scope.addressList = response.data;
            })
    };

    $scope.updateDefault = function (id) {
        baseService.sendGet("/user/updateDefault?id=" + id)
            .then(function (response) {
                if (response.data) {
                    $scope.findAddressByUser();
                } else {
                    alert("修改失败");
                }
            })
    };

    $scope.findProvinces = function () {
        baseService.sendGet("/user/findProvinces")
            .then(function (resopnse) {
                $scope.provinces = resopnse.data;
            })
    };

    $scope.findCities = function (provinceId) {
        baseService.sendGet("/user/findCities?provinceId=" + provinceId)
            .then(function (resopnse) {
                $scope.cities = resopnse.data;
            })
    };

    $scope.findCAreas = function (cityId) {
        baseService.sendGet("/user/findCAreas?cityId=" + cityId)
            .then(function (resopnse) {
                $scope.areas = resopnse.data;
            })
    };


    $scope.$watch('address.provinceId', function (newVal, oldVal) {
        if (newVal) {
            $scope.findCities(newVal);
        } else {
            $scope.cities = null;
        }
    });

    $scope.$watch('address.cityId', function (newVal, oldVal) {
        if (newVal) {
            $scope.findCAreas(newVal);
        } else {
            $scope.areas = null;
        }
    });

    /** 显示修改，为修改表单绑定当行数据 */
    $scope.show = function (address) {
        // 把entity的json对象转化成一个新的json对象
        $scope.address = JSON.parse(JSON.stringify(address));
    };

    /** 添加与修改地址 */
    $scope.saveOrUpdate = function () {
        /** 定义请求URL */
        var url = "saveAddress"; // 添加品牌
        if ($scope.address.id) {
            url = "updateAddress"; // 修改品牌
        }
        /** 调用服务层 */
        baseService.sendPost("/user/" + url, $scope.address)
            .then(function (response) {
                if (response.data) {
                    /** 重新加载地址数据 */
                    $scope.findAddressByUser();
                } else {
                    alert("操作失败");
                }
            });
    };

    $scope.deleteAddress = function (address) {
        if (address.isDefault != 1) {
            baseService.sendGet("/user/deleteAddress?id=" + address.id)
                .then(function (response) {
                    if (response.data) {
                        /** 重新加载地址数据 */
                        $scope.findAddressByUser();
                    } else {
                        alert("操作失败");
                    }
                })
        } else {
            alert("默认地址不能删除!");
        }
    }
});