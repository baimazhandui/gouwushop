app.controller("cartController", function
    ($scope, $controller, baseService) {
    // 指定继承baseController
    $controller("baseController", {$scope: $scope});

    // 添加商品到购物车
    $scope.addCart = function (sellerId, orderItem, num) {
        baseService.sendGet("/cart/addCart/?itemId="
            + orderItem.itemId + "&num=" + num)
            .then(function (response) {
                if (response.data) {
                    $scope.findCart();
                    for (var i in $scope.finalEntity[sellerId]) {
                        var item = $scope.finalEntity[sellerId][i];
                        if (item.itemId == orderItem.itemId) {
                            $scope.finalEntity[sellerId][i].num += num;
                            $scope.finalEntity[sellerId][i].totalFee =
                                (Math.round($scope.finalEntity[sellerId][i].totalFee * 100) +
                                    Math.round(orderItem.price * 100 * num)) / 100;
                            $scope.finalEntity.num += num;
                            $scope.finalEntity.totalMoney =
                                (Math.round($scope.finalEntity.totalMoney * 100) +
                                    Math.round(orderItem.price * 100 * num)) / 100;
                            if ($scope.finalEntity[sellerId][i].num <= 0) {
                                var idx = $scope.finalEntity[sellerId].indexOf(orderItem);
                                $scope.finalEntity[sellerId].splice(idx, 1);
                            }
                        }
                    }

                } else {
                    alert("操作失败");
                }
            });
    };

    // 手动输入添加商品到购物车
    $scope.updateOrderItem = function (sellerId, orderItem, num) {
        if (num > 0) {
            var num2 = num - (orderItem.totalFee / orderItem.price);
            $scope.addCart(sellerId, orderItem, num2)
        } else {
            alert("请输入正确的数量");
        }
    };

    $scope.cartList = [];
    // 查看购物车
    $scope.findCart = function () {
        baseService.sendGet("/cart/findCart")
            .then(function (response) {
                $scope.cartList = response.data;
                $scope.totalEntity = {totalNum : 0, totalMoney : 0.00};
                for (var i = 0; i < $scope.cartList.length; i++) {
                    var orderItems = $scope.cartList[i].orderItems;
                    for (var j = 0; j < orderItems.length; j++) {
                        var orderItem = orderItems[j];
                        var num = orderItem.num;
                        var totalFee = orderItem.totalFee;
                        $scope.totalEntity.totalNum += num;
                        $scope.totalEntity.totalMoney += totalFee;
                    }
                }
            })
    };

    // 定义最终订单
    $scope.finalEntity = {num : 0, totalMoney : 0.00};
    // 定义商品是否选中属性实体类(包含数组)
    $scope.checkedEntity = {};

    // 商品单选框点击事件
    $scope.selectOrderItem = function ($event, orderItem, i) {
        // 定义变量保存商家id
        var sellerId = orderItem.sellerId;
        // 如果是商品单选框先于商家全选框点击
        if (!$scope.finalEntity[sellerId]) {
            $scope.finalEntity[sellerId] = [];
            $scope.checkedEntity[sellerId] = [];
        }
        // 判断单选框是否选中
        if ($event.target.checked) {
            $scope.finalEntity[sellerId].push(orderItem);
            $scope.finalEntity.num += orderItem.num;
            $scope.finalEntity.totalMoney += orderItem.totalFee;
        } else {
            var idx = $scope.finalEntity[orderItem.sellerId].indexOf(orderItem);
            $scope.finalEntity[sellerId].splice(idx, 1);
            $scope.finalEntity.num -= orderItem.num;
            $scope.finalEntity.totalMoney -= orderItem.totalFee;
        }
        $scope.checkedEntity[sellerId][i] = $event.target.checked;
    };

    // 购物车商家全选框点击事件
    $scope.selectCart = function ($event, cart) {
        // 定义变量保存商家id
        var sellerId = cart.sellerId;
        // 定义选中商品数组
        var arr = [];
        var cartArr = getCartNumAndMoney(cart);
        var cartSelectedArr = getCartSelectedNumAndMoney(cart.sellerId);
        if ($event.target.checked) {
            $scope.finalEntity.num += (cartArr[0] - cartSelectedArr[0]);
            $scope.finalEntity.totalMoney += (cartArr[1] - cartSelectedArr[1]);
        } else {
            $scope.finalEntity.num -= cartArr[0];
            $scope.finalEntity.totalMoney -= cartArr[1];
        }
        // 定义该商家商品选中属性集合
        $scope.checkedEntity[sellerId] = [];
        // 迭代商家商品,先清除再根据全选框状态判断
        for (var i in cart.orderItems) {
            // 为商品单选框是否选中赋值
            $scope.checkedEntity[sellerId][i] = $event.target.checked;
            var orderItem = cart.orderItems[i];
            // 根据选中状态添加或其删除商家的所有商品
            if ($event.target.checked) {
                arr.push(orderItem);
            }
        }
        $scope.finalEntity[sellerId] = arr;
    };

    // 总全选框
    $scope.selectAll = function ($event) {
        // 清空最终订单数据
        $scope.finalEntity = {num : 0, totalMoney : 0.00};
        // 清空商品选中属性对象
        $scope.checkedEntity = {};
        // 遍历商家
        for (var i in $scope.cartList) {
            // 定义变量保存商家
            var cart = $scope.cartList[i];
            // 定义最终订单商家商品数组
            $scope.finalEntity[cart.sellerId] = [];
            // 定义商品选中属性数组
            $scope.checkedEntity[cart.sellerId] = [];
            for (var j in cart.orderItems) {
                // 为商品单选框是否选中赋值
                $scope.checkedEntity[cart.sellerId][j] = $event.target.checked;
                // 定义变量保存商品
                var orderItem = cart.orderItems[j];
                // 判断全选框是否选中
                if ($event.target.checked) {
                    // 选中则添加到数组中
                    $scope.finalEntity[cart.sellerId].push(orderItem);
                    // 商品数量和总价累加
                    $scope.finalEntity.num += orderItem.num;
                    $scope.finalEntity.totalMoney += orderItem.totalFee;
                }
            }
        }
    };

    // 定义获取商家商品总数
    var getCartNumAndMoney = function (cart) {
        var num = 0;
        var money = 0;
        for (var i in cart.orderItems) {
            num += cart.orderItems[i].num;
            money += cart.orderItems[i].totalFee;
        }
        return [num, money];
    };

    // 定义获取商家选择商品总数
    var getCartSelectedNumAndMoney = function (sellerId) {
        var num = 0;
        var money = 0;
        for (var i in $scope.finalEntity[sellerId]) {
            num += $scope.finalEntity[sellerId][i].num;
            money += $scope.finalEntity[sellerId][i].totalFee;
        }
        return [num, money];
    };

    // 结算点击事件
    $scope.addToOrder = function () {
        if ($scope.finalEntity.num) {
            // 定义选中商家存放数组
            $scope.finalEntity.selectedCartsIds = [];
            // 定义选中商品 id 存放数组
            $scope.finalEntity.selectedIds = [];
            for (var i in $scope.cartList) {
                var cart = $scope.cartList[i];
                if ($scope.finalEntity[cart.sellerId]) {
                    $scope.finalEntity.selectedCartsIds.push(cart.sellerId);
                    for (var j in $scope.finalEntity[cart.sellerId]) {
                        $scope.finalEntity.selectedIds.push($scope.finalEntity[cart.sellerId][j].itemId);
                    }
                }
            }
            var finalEntityStr = JSON.stringify($scope.finalEntity);
            location.href = "/order/getOrderInfo.html?entity=" + finalEntityStr;
        } else {
            alert("请选择商品再结算!");
        }
    };

});