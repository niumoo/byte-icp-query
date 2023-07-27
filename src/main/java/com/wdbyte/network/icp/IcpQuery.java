package com.wdbyte.network.icp;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.wdbyte.network.icp.entity.CheckImage;
import com.wdbyte.network.icp.entity.IcpInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hc.core5.http.ContentType;

/**
 * @author https://www.wdbyte.com
 * @date 2023/07/07
 */
@Slf4j
public class IcpQuery {

    public static void main(String[] args) {
        String queryInfo = "深圳市腾讯计算机系统有限公司";
        List<IcpInfo> icpInfoList = getIcpInfos(queryInfo);
        System.out.println(JSON.toJSONString(icpInfoList, Feature.PrettyFormat));
    }

    public static List<IcpInfo> getIcpInfos(String queryInfo) {
        // 获取 TOKEN
        String token = new IcpQuery().getToken();
        CheckImage checkImage = null;
        String sign = null;
        // 最多尝试3次
        for (int i = 0; i < 3; i++) {
            // 获取验证码
            checkImage = new IcpQuery().getCheckImage(token);
            // 验证验证码
            sign = new IcpQuery().checkImage(checkImage.getBigImage(), token, checkImage.getUuid());
            if (sign != null) {
                log.info("图片验证码验证通过，得到 sign:{}", sign);
                break;
            } else {
                log.info("图片验证码验证失败，重试....");
            }
        }
        // 查询备案信息
        return new IcpQuery().getIcpInfo(token, checkImage.getUuid(), sign, queryInfo);
    }

    /**
     * 备案网站请求验证 TOKEN
     *
     * @return
     */
    public String getToken() {
        String url = "https://hlwicpfwc.miit.gov.cn/icpproject_query/api/auth";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        headerMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        headerMap.put("Origin", "https://beian.miit.gov.cn");
        headerMap.put("Referer", "https://beian.miit.gov.cn");

        long timeStamp = System.currentTimeMillis();
        String authSecret = "testtest" + timeStamp;
        String authKey = DigestUtils.md5Hex(authSecret);
        authKey = String.format("authKey=%s&timeStamp=%d", authKey, timeStamp);

        try {
            log.info("开始获取备案网站 token....");
            String response = HttpUtil.post(url, authKey, ContentType.APPLICATION_FORM_URLENCODED, headerMap);
            String token = JSON.parseObject(response).getJSONObject("params").getString("bussiness");
            log.info("获取到网站 token : {}", token);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }

    /**
     * 请求获取验证码图片
     *
     * @param token
     * @return
     */
    public CheckImage getCheckImage(String token) {
        String url = "https://hlwicpfwc.miit.gov.cn/icpproject_query/api/image/getCheckImage";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json, text/plain, */*");
        headerMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        headerMap.put("Origin", "https://beian.miit.gov.cn");
        headerMap.put("Referer", "https://beian.miit.gov.cn");
        headerMap.put("token", token);
        try {
            log.info("开始请求图片验证码..");
            String response = HttpUtil.post(url, null, ContentType.APPLICATION_FORM_URLENCODED, headerMap);
            CheckImage checkImage = JSON.parseObject(response).getJSONObject("params").to(CheckImage.class);
            log.info("请求图片验证码成功，验证码 id:{}", checkImage.getUuid());
            return checkImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验证码校验
     *
     * @param base64Image
     * @param token
     * @param imgKey
     * @return
     */
    public String checkImage(String base64Image, String token, String imgKey) {
        String url = "https://hlwicpfwc.miit.gov.cn/icpproject_query/api/image/checkImage";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json, text/plain, */*");
        headerMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        headerMap.put("Origin", "https://beian.miit.gov.cn");
        headerMap.put("Referer", "https://beian.miit.gov.cn");
        headerMap.put("token", token);

        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        int index = CheckImageUtil.checkImage(decodedBytes);

        HashMap<String, String> postDataMap = new HashMap<>();
        postDataMap.put("key",imgKey);
        postDataMap.put("value",String.valueOf(index));

        log.info("开始验证图片验证码");
        String response = HttpUtil.post(url, JSON.toJSONString(postDataMap), ContentType.APPLICATION_JSON, headerMap);
        return JSON.parseObject(response).getString("params");
    }

    /**
     * 获取 ICP 备案信息
     * @param token
     * @param uuid
     * @param sign
     * @param queryInfo
     * @return
     */
    public List<IcpInfo> getIcpInfo(String token, String uuid, String sign, String queryInfo) {
        String url = "https://hlwicpfwc.miit.gov.cn/icpproject_query/api/icpAbbreviateInfo/queryByCondition";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json, text/plain, */*");
        headerMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        headerMap.put("Origin", "https://beian.miit.gov.cn");
        headerMap.put("Referer", "https://beian.miit.gov.cn");
        headerMap.put("token", token);
        headerMap.put("uuid", uuid);
        headerMap.put("sign", sign);

        int currentPage = 0;
        String lastPage = "0";
        List<IcpInfo> icpInfoList = new ArrayList<>();
        do {
            currentPage++;
            HashMap<String, String> postDataMap = new HashMap<>();
            postDataMap.put("pageNum", ""+currentPage);
            postDataMap.put("pageSize", "40");
            postDataMap.put("unitName", queryInfo);
            log.info("开始查询备案信息，postDataMap:{}", postDataMap);
            String response = HttpUtil.post(url, JSON.toJSONString(postDataMap), ContentType.APPLICATION_JSON, headerMap);
            lastPage = JSON.parseObject(response).getJSONObject("params").getString("lastPage");
            icpInfoList.addAll(JSON.parseObject(response).getJSONObject("params").getJSONArray("list").toList(IcpInfo.class));
            try {
                Thread.sleep(new Random().nextInt(5) * 1000);
            } catch (InterruptedException e) {
                log.error("sleep InterruptedException....");
            }
        } while (Integer.valueOf(lastPage) > currentPage);
        return icpInfoList;
    }

}
