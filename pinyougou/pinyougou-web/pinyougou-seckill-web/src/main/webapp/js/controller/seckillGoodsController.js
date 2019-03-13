/** 定义秒杀商品控制器 */
app.controller("seckillGoodsController", function($scope,$controller,$location,$timeout,baseService){

    /** 指定继承cartController */
    $controller("baseController", {$scope:$scope});

    // 查询秒杀商品列表数据
    $scope.findSeckillGoods = function () {
        baseService.sendGet("/seckill/findSeckillGoods")
            .then(function (response) {
                $scope.seckillGoodsList = response.data;
            })
    };

    // 根据秒杀商品 id 查询对应商品
    $scope.findOne = function () {
        // 获取请求 URL 后面的参数
        var id = $location.search().id;
        baseService.sendGet("/seckill/findOne?id=" + id)
            .then(function (response) {
                $scope.entity = response.data;
                // 调用倒计时方法
                $scope.countdown($scope.entity.endTime);
            })
    };

    // 定义倒计时方法
    $scope.countdown = function (endTime) {
        // 计算出相差的毫秒数
        var milliSeconds = new Date(endTime)
            .getTime() - new Date().getTime();
        // 计算出相差的秒数
        var seconds = Math.floor(milliSeconds / 1000);
        // 判断秒是否大于0
        if (seconds > 0) {
            // 计算出分钟
            var minutes = Math.floor(seconds / 60);
            // 计算出小时
            var hours = Math.floor(minutes / 60);
            // 计算出天数
            var days = Math.floor(hours / 24);
            // 定义 resArr 封装最后显示的时间
            var resArr = new Array();

            if (days > 0) {
                resArr.push(days + "天");
            }
            if (hours > 0) {
                resArr.push((hours - days * 24) + ":");
            }
            if (minutes > 0) {
                resArr.push((minutes - hours * 60) + ":");
            }
            if (seconds > 0) {
                resArr.push(seconds - minutes * 60);
            }
            $scope.timeStr = resArr.join("");

            // 开启延时定时器
            $timeout(function () {
                $scope.countdown(endTime);
            }, 1000);
        } else {
            $scope.timeStr = "秒杀结束!";
        }
    };

    $scope.submitOrder = function () {
        if ($scope.loginName) { // 已登录
            baseService.sendGet("/seckill/submitOrder?id="
                + $scope.entity.id).then(function (response) {
                    if (response.data) {
                        location.href = "/order/pay.html";
                    } else {
                        alert("下单失败!");
                    }
                })
        } else { // 未登录
            // 跳转到单点登录系统
            location.href = "http://sso.pinyougou.com?service="
                + $scope.redirectUrl;
        }
    }
});