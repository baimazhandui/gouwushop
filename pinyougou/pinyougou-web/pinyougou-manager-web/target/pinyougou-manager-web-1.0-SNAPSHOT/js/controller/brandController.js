/** 品牌控制器层 */
app.controller("brandController", function ($scope, $controller, baseService) {
    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});
    /** 定义搜索对象 */
    $scope.searchEntity = {};
    /** 分页查询品牌信息 */
    $scope.search = function (page, rows) {
        /** 调用服务层分页查询品牌数据 */
        baseService.findByPage("/brand/findByPage", page,
            rows, $scope.searchEntity)
            .then(function (response) {
                $scope.dataList = response.data.rows;
                /** 更新总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
                $scope.ids = [];
                $scope.checkedArr = [];
                $scope.all = false;
            });
    };
    /** 添加与修改品牌 */
    $scope.saveOrUpdate = function () {
        /** 定义请求URL */
        var url = "save"; // 添加品牌
        if ($scope.entity.id) {
            url = "update"; // 修改品牌
        }
        /** 调用服务层 */
        baseService.sendPost("/brand/" + url, $scope.entity)
            .then(function (response) {
                if (response.data) {
                    /** 重新加载品牌数据 */
                    $scope.reload();
                } else {
                    alert("操作失败");
                }
            });
    };
    /** 显示修改，为修改表单绑定当行数据 */
    $scope.show = function (entity) {
        // 把entity的json对象转化成一个新的json对象
        $scope.entity = JSON.parse(JSON.stringify(entity));
    };
    /** 批量删除品牌 */
    $scope.delete = function () {
        if ($scope.ids.length > 0) {
            /** 调用服务层 */
            baseService.deleteById("/brand/delete", $scope.ids).then(
                function (response) {
                    if (response.data) {
                        /** 重新加载品牌数据 */
                        $scope.reload();
                    }
                }
            );
        } else {
            alert("请选择要删除的品牌！");
        }
    };

    /** 定义ids数组封装删除的id */
    $scope.ids = [];
    /** 定义checkbox点击事件函数 */
    $scope.changeSelection = function ($event, id, i) {
        /** 判断checkbox是否选中 */
        if ($event.target.checked) {
            $scope.ids.push(id);
        } else {
            /** 从数组中移除 */
            var idx = $scope.ids.indexOf(id);
            $scope.ids.splice(idx, 1);
        }
        $scope.checkedArr[i] = $event.target.checked;
        $scope.all = $scope.ids.length == $scope.dataList.length;
    };

    /** 全选框点击事件(全选反选) */
    $scope.selall = function ($event) {
        $scope.ids = [];
        for (var i in $scope.dataList) {
            $scope.checkedArr[i] = $event.target.checked;
            if ($event.target.checked) {
                $scope.ids.push($scope.dataList[i].id);
            }
        }
        $scope.all = $scope.ids.length == $scope.dataList.length;
    }
});
// 说明：$controller也是AngularJS提供的一个服务，可以实现伪继承，实际上就是与baseController共享$scope。