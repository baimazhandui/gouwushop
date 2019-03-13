package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.mapper.AddressMapper;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.Address;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service(interfaceName = "com.pinyougou.service.UserService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${sms.url}")
    private String smsUrl;

    @Value("${sms.signName}")
    private String signName;

    @Value("${sms.templateCode}")
    private String templateCode;

    /**
     * 添加方法
     *
     * @param user
     */
    @Override
    public void save(User user) {
        try {
            // 创建日期
            user.setCreated(new Date());
            // 修改日期
            user.setUpdated(user.getCreated());
            // 密码加密
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            userMapper.insertSelective(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改方法
     *
     * @param user
     */
    @Override
    public void update(User user) {
        userMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 根据主键id删除
     *
     * @param id
     */
    @Override
    public void delete(Serializable id) {

    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void deleteAll(Serializable[] ids) {

    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public User findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<User> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param user
     * @param page
     * @param rows
     */
    @Override
    public List<User> findByPage(User user, int page, int rows) {
        return null;
    }

    /**
     * 根据电话号码发送短信验证码
     *
     * @param phone
     */
    @Override
    public boolean sendCode(String phone) {
        try {
            // 生成 6 为随机数
            String code = UUID.randomUUID().toString()
                    .replaceAll("-", "")
                    .replaceAll("[a-z|A-Z]", "")
                    .substring(0, 6);

            System.out.println("验证码: " + code);
            // 调用短信发送接口
            HttpClientUtils httpClientUtils = new HttpClientUtils(false);
            // 创建 Map 集合封装请求参数
            Map<String, String> param = new HashMap<>();
            param.put("phone", phone);
            param.put("signName", signName);
            param.put("templateCode", templateCode);
            //param.put("templateParam", "{\"code\" : \"" + code + "\"}");
            param.put("templateParam", "{\"code\":\"" + code + "\"}");
            // 发送 Post 请求
            String content = httpClientUtils.sendPost(smsUrl, param);
            // 把 json 字符串转化成Map
            Map<String, Object> resMap = JSON.parseObject(content, Map.class);
            // 存入 redis 中(90秒)
            redisTemplate.boundValueOps(phone).set(code, 90, TimeUnit.SECONDS);
            return (boolean) resMap.get("success");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验验证码
     *
     * @param phone
     * @param smsCode
     */
    @Override
    public boolean checkSmsCode(String phone, String smsCode) {
        // 获取 Redis 中存储的验证码
        String code = redisTemplate.boundValueOps(phone).get();
        return StringUtils.isNoneBlank(code) && code.equals(smsCode);
    }

    @Override
    public User findUser(String username) {
        try {
            User user = new User();
            user.setUsername(username);
            return userMapper.select(user).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据用户id查询地址列表
     *
     * @param userId
     */
    @Override
    public List<Address> findAddressByUser(String userId) {
        Address address = new Address();
        address.setUserId(userId);
        return addressMapper.select(address);
    }

    /**
     * 根据用户id和地址id修改默认状态
     *  @param username
     * @param id
     */
    @Override
    public void updateDefault(String username, Long id) {
        try {
            Example example = new Example(Address.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("userId", username);
            Address address = new Address();
            address.setIsDefault("0");
            addressMapper.updateByExampleSelective(address, example);
            address.setIsDefault("1");
            address.setId(id);
            addressMapper.updateByPrimaryKeySelective(address);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加地址
     *
     * @param address
     */
    @Override
    public void saveAddress(Address address) {
        addressMapper.insertSelective(address);
    }

    /**
     * 修改地址
     *
     * @param address
     */
    @Override
    public void updateAddress(Address address) {
        addressMapper.updateByPrimaryKeySelective(address);
    }

    /**
     * 根据地址id删除指定地址
     *
     * @param id
     */
    @Override
    public void deleteAddress(Long id) {
        addressMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void saveMsg(String username, String phone) {
        User user = new User();
        user.setUsername(username);
        User user1 = userMapper.select(user).get(0);
        user1.setPhone(phone);
        userMapper.updateByPrimaryKeySelective(user1);
    }

}
