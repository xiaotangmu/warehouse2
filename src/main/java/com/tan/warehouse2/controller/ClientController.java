package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Client;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.service.ClientService;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    ClientService clientService;

    @GetMapping("/getAll")
    public Object getAll(){
        return Msg.success(clientService.getAll2());
    }

    @PostMapping("/delete")
    @RequiresPermissions("client:delete")
    public Object delete(String names, String ids){
        if(StringUtils.isNotBlank(names) && StringUtils.isNotBlank(ids)){
            List<Integer> integers = MyStrUtil.intSplit(ids, "-");
            List<String> strings = MyStrUtil.strSplit(names, "-");
            clientService.delete(strings, integers);
            return Msg.success("删除成功！");
        }
        return Msg.failError("删除失败");
    }

    @PostMapping("/update")
    @RequiresPermissions("client:update")
    public Object update(Client client){
        System.out.println("update ..." + client);
        if (StringUtils.isNotBlank(client.getName()) && client.getId() != null && client.getId() > 0){
            int update = clientService.update(client);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }

    @PostMapping("/find")
//    @GetMapping("/findClient")
    public Object findClient(String name, Integer pageNum, Integer pageSize){
        return Msg.success(clientService.getClientLikeName(name, pageNum, pageSize));
    }

    @GetMapping("/getAllPage")
    public Object getAllPage(Integer pageNum, Integer pageSize){
        return Msg.success(clientService.getAllPage(pageNum, pageSize));
    }

    @PostMapping("/add")
    @RequiresPermissions("client:add")
    public Object add(Client client){//添加品牌

        System.out.println(client);

        //判断name 是否为空
        if(StringUtils.isBlank(client.getName())){
            return Msg.noCondition("品牌名不能为空");
        }
        //判断名字是否重复
        String name = client.getName();
        Client client2 = clientService.getClientByName(name);
        if (client2 != null){
            return Msg.noCondition("该品牌已经存在");
        }
        int add = clientService.add(client);
        if (add != 0 && add != -1) {
            if (client.getId() != null) {
                return Msg.success(client);
            }
        }else if (add == -1){
            return Msg.noCondition("该品牌已经存在");
        }
        return Msg.failError("添加失败！");
    }

    @PostMapping("/nameCheck")
//    @GetMapping("/nameCheck")
    public Object nameCheck(String name){
        System.out.println(name);
        if (StringUtils.isBlank(name)) {
            return Msg.failError("品牌名不能为空！");
        }
        Client client = clientService.getClientByName(name);
        if(client == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

}
