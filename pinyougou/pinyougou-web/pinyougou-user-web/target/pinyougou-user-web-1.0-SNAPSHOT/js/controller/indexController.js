// 首页控制器
app.controller("indexController", function ($scope, $controller, $location, $interval, $filter, baseService) {

    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});

    /** 分页查询品牌信息 */
    /*$scope.search = function (page, rows) {
        /!** 调用服务层分页查询品牌数据 *!/
        baseService.findByPage("/order/findByPage", page, rows)
            .then(function (response) {
                $scope.dataList = response.data.rows;
                console.log($scope.dataList);
                /!** 更新总记录数 *!/
                $scope.paginationConf.totalItems = response.data.total;

                for (var i in $scope.dataList) {
                    $scope.dataList[i].updateTime =
                        $filter('date')($scope.dataList[i].updateTime, "yyyy-MM-dd")
                }
            });
    };*/

    $scope.pay = function (orders) {
        var ordersStr = JSON.stringify(orders);
        location.href = "home-order-pay.html?ordersStr=" + ordersStr;
    };

    // 获取支付总金额
    $scope.getOrdersStr = function () {
        return $location.search().ordersStr;
    };

    $scope.getOrders = function () {
        $scope.orders = JSON.parse($scope.getOrdersStr());
    };

    $scope.payOrders = function (orderId, totalFee) {
        location.href = "pay.html?orderId=" + orderId + "&totalFee=" + totalFee;
    };

    $scope.getOrderId = function () {
        return $location.search().orderId;
    };

    $scope.getTotalFee = function () {
        return $location.search().totalFee;
    };

    $scope.getToPay = function () {
        $scope.orderId = $scope.getOrderId();
        $scope.totalFee = $scope.getTotalFee();
    };


    // 生成微信支付二维码
    $scope.genPayCode = function () {
        baseService.sendGet("/order/genPayCode?orderId=" + $scope.orderId)
            .then(function (response) {
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
                        location.href = "paysuccess.html?money="
                            + $scope.money;
                    }
                    if (response.data.status == 3) { // 支付失败
                        // 取消定时器
                        $interval.cancel(timer);
                        location.href = "payfail.html";
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

    $scope.page = 1;
    $scope.rows = 10;


    $scope.findByPage = function () {
        /** 调用服务层分页查询品牌数据 */
        baseService.findByPage("/order/findByPage", $scope.page, $scope.rows)
            .then(function (response) {
                $scope.dataList = response.data.rows;
                /** 更新总记录数 (total拿不到) */
                $scope.paginationConf.totalItems = 7;

                for (var i in $scope.dataList) {
                    $scope.dataList[i].updateTime =
                        $filter('date')($scope.dataList[i].updateTime, "yyyy-MM-dd")
                }

                initPageNum();
            });
    };


    // 定义初始化页码方法
    var initPageNum = function () {
        // 定义页码数组
        $scope.pageNums = [];
        // 获取总页码
        $scope.totalPages = $scope.paginationConf.totalItems;
        // 开始页码
        /*var firstPage = 1;*/
        $scope.firstPage = 1;
        // 结束页码
        $scope.lastPage = $scope.totalPages;
        // 前面有点
        $scope.preDot = false;
        // 后面有点
        $scope.postDot = false;
        // 如果总页数大于5,显示部分页码
        if ($scope.totalPages > 5) {
            // 如果当前页码处于前面位置
            if ($scope.page <= 3) {
                $scope.lastPage = 5; // 生成前 5 页页码
                $scope.postDot = true;
            }
            // 如果当前页码处于后面位置
            else if ($scope.page >= $scope.totalPages - 3) {
                $scope.firstPage = $scope.totalPages - 4; // 生成后 5 页页码
                $scope.preDot = true;
            } else {
                $scope.firstPage = $scope.page - 2;
                $scope.lastPage = $scope.page + 2;
            }
        }
        // 循环产生页码
        for (var i = $scope.firstPage; i <= $scope.lastPage; i++) {
            $scope.pageNums.push(i);
        }
    };

    $scope.pageSearch = function (page) {
        if (page > 0 && page <= $scope.paginationConf.totalItems) {
            $scope.page = Number(page);
            $scope.findByPage();
        } else {
            return;
        }
    };
});