// 商品详情页控制器
app.controller("itemController", function ($scope, $controller, $http) {
    // 指定继承baseController
    $controller("baseController", {$scope : $scope});

    // 定义选择商品数量方法
    $scope.addNum = function (num) {
        $scope.num += num;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    };

    // 定义用户选择的规格选项
    $scope.selectedSpec = {};
    // 定义规格选中方法
    $scope.selectSpec = function (name, value) {
        $scope.selectedSpec[name] = value;
        // 查找对应的 SKU 商品
        searchSKU();
    };

    // 定义规格是否选中方法
    $scope.isSelected = function (name, value) {
        return $scope.selectedSpec[name] == value;
    };

    // 加载默认的SKU
    $scope.loadSku = function () {
        // 取第一个SKU
        $scope.sku = itemList[0];
        // 获取SKU商品选择的选项规格(JSON字符串转JSON对象)
        $scope.selectedSpec = JSON.parse($scope.sku.spec);
    };

    var searchSKU = function () {
        for (var i =0; i < itemList.length; i++) {
            if (JSON.stringify($scope.selectedSpec) == itemList[i].spec) {
                $scope.sku = itemList[i];
                return;
            }
        }
    };

    // 加入购物车事件绑定
    $scope.addToCart = function(){
        $http.get("http://cart.pinyougou.com/cart/addCart?itemId="
            + $scope.sku.id + "&num=" + $scope.num,
            {"withCredentials" : true})
            .then(function (response) {
                if (response.data) {
                    // 跳转到购物车页面
                    location.href = "http://cart.pinyougou.com/cart.html";
                } else {
                    alert("请求失败!");
                }
            })
    };

});