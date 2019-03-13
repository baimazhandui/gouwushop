/** 定义控制器层 */
app.controller('goodsController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});

    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function(page, rows){
        baseService.findByPage("/goods/findByPage", page,
			rows, $scope.searchEntity)
            .then(function(response){
                /** 获取分页查询结果 */
                $scope.dataList = response.data.rows;
                /** 更新分页总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
            });
    };

    /** 添加或修改 */
    $scope.saveOrUpdate = function(){
        // 获取富文本编辑器的内容
        $scope.goods.goodsDesc.introduction = editor.html();
        /** 发送post请求 */
        baseService.sendPost("/goods/save", $scope.goods)
            .then(function(response){
                if (response.data){
                    alert("保存成功!");
                    $scope.goods = null;
                    editor.html("");
                }else{
                    alert("操作失败！");
                }
            });
    };

    /** 显示修改 */
    $scope.show = function(entity){
       /** 把json对象转化成一个新的json对象 */
       $scope.entity = JSON.parse(JSON.stringify(entity));
    };

    /** 批量删除 */
    $scope.delete = function(){
        if ($scope.ids.length > 0){
            baseService.deleteById("/goods/delete", $scope.ids)
                .then(function(response){
                    if (response.data){
                        /** 重新加载数据 */
                        $scope.reload();
                    }else{
                        alert("删除失败！");
                    }
                });
        }else{
            alert("请选择要删除的记录！");
        }
    };
    /*图片上传方法**/
    $scope.uploadFile = function () {
        baseService.uploadFile()
            .then(function (response) {
                if (response.data.status == 200) {
                    $scope.picEntity.url = response.data.url;
                }
            })
    };


    $scope.addPic = function () {
        $scope.goods.goodsDesc.itemImages.push($scope.picEntity);
    };

    $scope.removePic = function (index) {
        $scope.goods.goodsDesc.itemImages.splice(index, 1);
    };
    
    $scope.findItemCatByParentId = function (parentId, name) {
        baseService.sendGet("/itemCat/findItemCatByParentId?parentId=" + parentId)
            .then(function (response) {
                $scope[name] = response.data;
            })
    };

    $scope.$watch('goods.category1Id', function (newVal, oldVal) {
        if (newVal) {
            $scope.findItemCatByParentId(newVal, 'itemCatList2')
        } else {
            $scope.itemCatList2 = null;
        }
    });

    $scope.$watch('goods.category2Id', function (newVal, oldVal) {
       if (newVal) {
           $scope.findItemCatByParentId(newVal, 'itemCatList3');
       } else {
           $scope.itemCatList3 = null;
       }
    });

    $scope.$watch('goods.category3Id', function (newVal, oldVal) {
        if (newVal) {
            for (var i = 0; i < $scope.itemCatList3.length; i++) {
                var item = $scope.itemCatList3[i];
                if (item.id == newVal) {
                    $scope.goods.typeTemplateId = item.typeId;
                    break;
                }
            }
        } else {
            $scope.goods.typeTemplateId = "";
        }
    });

    $scope.$watch('goods.typeTemplateId', function (newVal, oldVal) {
        if (newVal) {
            baseService.findOne("/typeTemplate/findOne", newVal)
                .then(function (response) {
                    // 获取品牌列表
                    // console.log(response);
                    $scope.brandIds = JSON.parse(response.data.brandIds);
                    // 设置拓展属性
                    $scope.goods.goodsDesc.customAttributeItems = JSON.parse(response.data.customAttributeItems);

                });
            // specificationItems: [{"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"},{"attributeValue":["64G","128G"],"attributeName":"机身内存"}]
            // 根据模板id 查询对应规格
            baseService.findOne("/typeTemplate/findSpecByTemplateId", $scope.goods.typeTemplateId)
                .then(function (response) {
                    $scope.specList = response.data;
                    // $scope.optionList = response.data.options; 未定义???
                    // console.log($scope.optionList);
                })
        } else {
            $scope.brandIds = null;
            $scope.goods.goodsDesc.customAttributeItems = [];
            $scope.goods.goodsDesc.specificationItems = [];
            $scope.specList = null;
        }
    });

    // 定义对象goods
    $scope.goods = {goodsDesc:{itemImages : [], specificationItems : []}};

    $scope.updateSpecAttr = function ($event, name, value) {
        // 根据json对象的key到json数组中查询对应的key的对象
        console.log($scope.goods);
        var obj =
            $scope.searchJsonByKey($scope.goods.goodsDesc.specificationItems, "attributeName", name);
        // 存在该对象
        if (obj) {
            // 判断checkbox是否选中
            if ($event.target.checked) {
                // 选中添加到数组中
                obj.attributeValue.push(value);
            } else {
                // 取消勾选,从数组中删除
                obj.attributeValue.splice(obj.attributeValue.indexOf(value), 1);
                // 如果全部取消则删除该对象
                if (obj.attributeValue.length == 0) {
                    $scope.goods.goodsDesc.specificationItems
                        .splice($scope.goods.goodsDesc.specificationItems.indexOf(obj), 1);
                }
            }
        } else {
            // 不存在该对象,则新增数组元素
            $scope.goods.goodsDesc.specificationItems
                .push({"attributeName": name, "attributeValue": [value]});
        }
    };

    /** 创建SKU商品方法 */
    $scope.createItems = function(){
        /** 定义SKU数组，并初始化 */
        $scope.goods.items = [{spec:{}, price:0, num:9999,
            status:'0', isDefault:'0' }];
        /** 定义选中的规格选项数组 */
        var specItems = $scope.goods.goodsDesc.specificationItems;
        /** 循环选中的规格选项数组 */
        for(var i = 0; i < specItems.length; i++){
            /** 扩充原SKU数组方法 */
            $scope.goods.items = swapItems($scope.goods.items,
                specItems[i].attributeName,
                specItems[i].attributeValue);
        }
    };
    /** 扩充SKU数组方法 */
    var swapItems = function(items, attributeName, attributeValue){
        /** 创建新的SKU数组 */
        var newItems = new Array();
        /** 迭代旧的SKU数组，循环扩充 */
        for(var i = 0; i < items.length; i++){
            /** 获取一个SKU商品 */
            var item = items[i];
            /** 迭代规格选项值数组 */
            for(var j = 0; j < attributeValue.length; j++){
                /** 克隆旧的SKU商品，产生新的SKU商品 */
                var newItem = JSON.parse(JSON.stringify(item));
                /** 增加新的key与value */
                newItem.spec[attributeName] = attributeValue[j];
                /** 添加到新的SKU数组 */
                newItems.push(newItem);
            }
        }
        return newItems;
    };

    /** 定义商品状态数组 */
    $scope.status = ['未审核','已审核','审核未通过','关闭'];

    $scope.updateMarketable = function (isMarketable) {
        if ($scope.ids.length > 0) {
            baseService.sendPost("/goods/updateMarketable?isMarketable=" + isMarketable, $scope.ids)
                .then(function (response) {
                    if (response.data) {
                        $scope.reload();
                        $scope.ids = [];
                    } else {
                        alert("操作失败");
                    }
                })
        } else {
            alert("请添加要操作的对象");
        }
    }
});