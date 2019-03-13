/** 定义基础控制器层 */
app.controller('baseController',function($scope){
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
    /** 定义ids数组封装删除的id */
    $scope.ids = [];
    /** 定义checkbox点击事件函数 */
    $scope.updateSelection = function($event, id){
        /** 判断checkbox是否选中 */
        if ($event.target.checked){
            $scope.ids.push(id);
        }else{
            /** 从数组中移除 */
            var idx = $scope.ids.indexOf(id);
            $scope.ids.splice(idx, 1);
        }
    };
    
    $scope.jsonArrToStr = function (jsonArrStr, key) {
        // 将json字符串jsonArrStr转成json对象(json数组)
        var jsonArr = JSON.parse(jsonArrStr);
        var arr = [];
        for (var i = 0; i < jsonArr.length; i ++) {
            var json = jsonArr[i];
            arr.push(json[key]);
        }
        return arr.join(",");
    };
});
