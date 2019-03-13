/** 定义搜索控制器 */
app.controller("searchController", function ($scope, $sce, $location, $controller, baseService) {
    // 指定继承baseController
    $controller("baseController", {$scope : $scope});

    $scope.pageNum = [];

        // 定义搜素方法
    $scope.search = function () {
        baseService.sendPost("/Search", $scope.searchParams)
            .then(function (response) {
                $scope.resultMap = response.data;
                // 初始化页码
                initPageNum();
            })
    };

    // 将文本转化成html
    $scope.trustHtml = function (html) {
        return $sce.trustAsHtml(html);
    };

    // 定义搜索参数对象
    $scope.searchParams = {category : "", brand : "", price : "",
        spec : {}, page : 1, rows : 20, sortField : "", sort : ""};

    // 定义集合存储搜索对象属性
    $scope.list = [];

    // 定义英汉映射对象
    $scope.map = {category : "商品分类", brand : "品牌",
        price : "价格", 网络 : "网络", 机身内存 : "机身内存"};

    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchParams[key] = value;
            $scope.temList = [];
            if (key == 'category') {
                $scope.temList.push({key : key, value : value});
                if ($scope.list.length > 0 ) {
                    for (var i = 0; i < $scope.list.length; i++) {
                        var obj = $scope.list[i];
                        $scope.temList.push(obj);
                    }
                }
                $scope.list = $scope.temList;
            } else {
                $scope.list.push({key : key, value : value});
            }
        } else {
            $scope.searchParams.spec[key] = value;
            $scope.list.push({key : key, value : value});
        }
        $scope.initParams();
        $scope.search();
    };

    $scope.deleteItem = function (idx, key) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchParams[key] = "";
        } else {
            delete $scope.searchParams.spec[key];
            //$scope.searchParams.spec[key] = "";
        }
        $scope.list.splice(idx, 1);
        $scope.initParams();
        $scope.search();
    };

    // 定义初始化页码方法
    var initPageNum = function () {
        // 定义页码数组
        $scope.pageNums = [];
        // 获取总页码
        var totalPages = $scope.resultMap.totalPages;
        // 开始页码
        var firstPage = 1;
        // 结束页码
        var lastPage = totalPages;
        // 前面有点
        $scope.preDot = false;
        // 后面有点
        $scope.postDot = false;
        // 如果总页数大于5,显示部分页码
        if (totalPages > 5) {
            // 如果当前页码处于前面位置
            if ($scope.searchParams.page <= 3) {
                lastPage = 5; // 生成前 5 页页码
                $scope.postDot = true;
            }
            // 如果当前页码处于后面位置
            else if ($scope.searchParams.page >= totalPages - 3) {
                firstPage = totalPages - 4; // 生成后 5 页页码
                $scope.preDot = true;
            } else {
                firstPage = $scope.searchParams.page - 2;
                lastPage = $scope.searchParams.page + 2;
            }
        }
        // 循环产生页码
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageNums.push(i);
        }
    };

    $scope.pageSearch = function (page) {
        if (page > 0 && page <=$scope.resultMap.totalPages) {
            $scope.searchParams.page = Number(page);
            $scope.search();
        } else {
            return;
        }
    };

    $scope.sortedSearch = function (sortField, sort) {
        $scope.searchParams.sortField = sortField;
        $scope.searchParams.sort = sort;
        $scope.search();
    };

    $scope.initParams = function () {
        $scope.searchParams.sortField = "";
        $scope.searchParams.sort = "";
        $scope.searchParams.page = 1;
    };

    $scope.getKeywords = function () {
        $scope.searchParams.keywords = $location.search().keywords;
        $scope.search();
    }
});
