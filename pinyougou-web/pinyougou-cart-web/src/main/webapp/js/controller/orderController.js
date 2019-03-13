// 定义订单控制器
app.controller("orderController", function ($scope, $controller, $interval, $location, baseService) {
    // 指定继承baseController
    $controller("cartController", {$scope : $scope});
    // 根据登录用户获取地址
    $scope.findAddressByUser = function () {
        baseService.sendGet("/order/findAddressByUser")
            .then(function (response) {
                $scope.addressList = response.data;
                for (var i in response.data) {
                    if (response.data[i].isDefault == 1) {
                        $scope.address = response.data[i];
                        break;
                    }
                }
                // 获取最终分订单实体对象(字符串)
                var entityStr = $location.search().entity;
                $scope.entity = JSON.parse(entityStr);
                console.log($scope.entity);
            })
    };
    // 选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    };

    // 判断是否是当前选中的地址
    $scope.isSelectedAddress = function (address) {
        return address == $scope.address;
    };


    // 定义 order 对象封装参数
    $scope.order = {paymentType : '1'};
    // 选择支付方式
    $scope.selectPayType = function (payType) {
        $scope.order.paymentType = payType;
    };

    // 保存订单
    $scope.saveOrder = function () {
        // 设置收件人地址
        $scope.order.receiverAreaName = $scope.address.address;
        // 设置收件人手机号码
        $scope.order.receiverMobile = $scope.address.mobile;
        // 设置收件人
        $scope.order.receiver = $scope.address.contact;
        // 发送异步请求
        baseService.sendPost("/order/save?&itemIds=" + $scope.entity.selectedIds, $scope.order)
            .then(function (response) {
                if (response.data) {
                    // 如果是微信支付，跳转到扫码支付页面
                    if ($scope.order.paymentType == 1){
                        location.href = "/order/pay.html";
                    }else{
                        // 如果是货到付款，跳转到成功页面
                        location.href = "/order/paysuccess.html";
                    }
                } else {
                    alert("订单提交失败!");
                }
            })
    };

    // 生成微信支付二维码
    $scope.genPayCode = function () {
        baseService.sendGet("/order/genPayCode").then(function (response) {
            // 获取金额
            $scope.money = (response.data.totalFee / 100).toFixed(2);
            // 获取订单交易号
            $scope.outTradeNo = response.data.outTradeNo;
            // 生成二维码
            /*var qr = new QRious({
                element : document.getElementById("grious"),
                size : 200,
                level : "H",
                value : response.data.codeUrl
            });*/

            // 用zxing生成二维码
            document.getElementById("qrious").src = "/barcode?url=" + response.data.codeUrl;
            /**
             * 开启定时器
             * 第一个参数: 调用的函数
             * 第二个参数: 时间毫秒数(3000毫秒也就是 3 秒)
             * 第三个参数: 调用的总次数(60次)
             */
            var timer = $interval(function () {
                baseService.sendGet("/order/queryPayStatus?outTradeNo="
                    + $scope.outTradeNo).then(function (response) {
                        if (response.data.status == 1) { // 支付成功
                            // 取消定时器
                            $interval.cancel(timer);
                            location.href = "/order/paysuccess.html?money="
                                + $scope.money;
                        }
                        if (response.data.status == 3) { // 支付失败
                            // 取消定时器
                            $interval.cancel(timer);
                            location.href = "/order/payfail.html";
                        }
                })
            }, 3000, 60);

            // 执行 60 次(3 分钟)之后需要回调的函数
            timer.then(function () {
                alert("微信支付二维码失效");
                /*$scope.promptMessage = "二维码已过期，刷新页面重新获取二维码。";*/
            })
        });
    };

    // 获取支付总金额
    $scope.getMoney = function () {
        return $location.search().money;
    };

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
        baseService.sendPost("/order/" + url, $scope.address)
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
            baseService.sendGet("/order/deleteAddress?id=" + address.id)
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