package com.zsg.jx.lightcontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 灯泡列表  用于转换成json字符串 存入表中
 * Created by zsg on 2016/8/24.
 */
public class LightList implements Serializable {
   public LinkedList<Light> list;

   public LightList(){
      list=new LinkedList<>();
   }

   public int getSize(){
      return list.size();
   }
}
