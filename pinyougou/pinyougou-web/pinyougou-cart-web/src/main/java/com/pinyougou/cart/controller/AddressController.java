package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Address;
import com.pinyougou.service.AddressService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/order")
public class AddressController {

    @Reference(timeout = 10000)
    private AddressService addressService;

    @GetMapping("/findAddressByUser")
    public List<Address> findAddressByUser(HttpServletRequest request) {
        String userId = request.getRemoteUser();
        return addressService.findAddressByUser(userId);
    }

    @PostMapping("/saveAddress")
    public boolean saveAddress(@RequestBody Address address, HttpServletRequest request) {
        try {
            address.setUserId(request.getRemoteUser());
            addressService.saveAddress(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @PostMapping("/updateAddress")
    public boolean updateAddress(@RequestBody Address address) {
        try {
            addressService.updateAddress(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/deleteAddress")
    public boolean deleteAddress(Long id) {
        try {
            addressService.deleteAddress(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
