package com.x.imagesearchmcpserver.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片搜索工具类
 * <p>通过 Pexels API 搜索高质量免费图片，返回图片URL、摄影师、尺寸等信息</p>
 *
 * @see <a href="https://www.pexels.com/api/documentation/#photos-search">Pexels API Docs</a>
 */
@Service
public class ImageSearchTool {

    private static final String PEXELS_SEARCH_API_URL = "https://api.pexels.com/v1/search";

    //@Value("${pexels.api.key}")
    private String apiKey = "Cz6PJE1Y8NhqxTrDiLA5i86QrziFagE7zJDc7nApKYO2amSNvTWolfMC";

    /**
     * 根据关键词搜索图片
     *
     * @param query   搜索关键词，例如 "nature"、"city"
     * @param perPage 返回结果数量，默认10，最大30
     * @param page    页码，默认1
     * @return 图片信息列表（JSON格式），包含图片URL、摄影师、尺寸等
     */
    @Tool(description = "Search for high-quality free images by keyword from Pexels. Returns image URLs, photographer, and dimensions.")
    public String searchImages(
            @ToolParam(description = "Search query keyword, e.g. 'nature', 'city'") String query,
            @ToolParam(description = "Number of results to return, default 10, max 30") Integer perPage,
            @ToolParam(description = "Page number, default 1") Integer page) {
        if (perPage == null || perPage < 1) {
            perPage = 10;
        }
        if (perPage > 30) {
            perPage = 30;
        }
        if (page == null || page < 1) {
            page = 1;
        }

        try {
            HttpResponse response = HttpRequest.get(PEXELS_SEARCH_API_URL)
                    .header("Authorization", apiKey)
                    .form("query", query)
                    .form("per_page", perPage)
                    .form("page", page)
                    .execute();

            if (!response.isOk()) {
                return "Failed to fetch images from Pexels API. HTTP status: " + response.getStatus();
            }

            String body = response.body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray photos = jsonObject.getJSONArray("photos");

            if (photos == null || photos.isEmpty()) {
                return "No images found for query: " + query;
            }

            int totalResults = jsonObject.getInt("total_results", 0);
            List<Object> resultList = photos.subList(0, Math.min(perPage, photos.size()));

            String imageResults = resultList.stream()
                    .map(obj -> {
                        JSONObject photo = (JSONObject) obj;
                        JSONObject src = photo.getJSONObject("src");
                        JSONObject result = new JSONObject();
                        result.set("id", photo.getInt("id"));
                        result.set("url", src.getStr("large"));
                        result.set("original", src.getStr("original"));
                        result.set("photographer", photo.getStr("photographer"));
                        result.set("width", photo.getInt("width"));
                        result.set("height", photo.getInt("height"));
                        result.set("alt", photo.getStr("alt"));
                        result.set("avg_color", photo.getStr("avg_color"));
                        return result.toString();
                    })
                    .collect(Collectors.joining("\n"));

            return "Total results: " + totalResults + ", Page: " + page + "\n" + imageResults;
        } catch (Exception e) {
            return "Error searching images: " + e.getMessage();
        }
    }
}