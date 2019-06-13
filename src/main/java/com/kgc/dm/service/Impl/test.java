package com.kgc.dm.service.Impl;

import com.kgc.dm.common.RedisUtils;
import com.kgc.dm.exception.User;
import com.kgc.dm.pojo.DmItem;
import com.kgc.dm.pojo.DmOrder;
import com.kgc.dm.pojo.DmUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.*;

public class test {

    @Autowired
    RedisUtils redisUtils;

    public test(){

        redisUtils.delete("token");
    }


    public static void main(String[] args) {
        //String s = "ajosj";
        //String a = "a";
        //String b = "b";
        //String c = "c";
        //String abc = "abc";
        //String s1 = "ssaa";
        //System.out.println(a.hashCode()+"/"+b.hashCode()+"/"+c.hashCode()+"/"+abc.hashCode());
        List<User> objects = new ArrayList<>();
        objects.add(new User("zhangsan", 22));
        objects.add(new User("lijin", 22));
        objects.add(new User("lijin",22));
        objects.sort(Comparator.comparing(User::getName));
        System.out.println(objects.get(0).getName()+"/"+objects.get(1).getName());


    }
}
