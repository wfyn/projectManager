import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WechatAuthorizerExport {

    // API配置
    private static final String API_URL = "https://www.szszcloud.cn/wxcomponent/admin/authorizer-list";
    private static final String AUTHORIZATION = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVc2VyTmFtZSI6InJvb3QiLCJpc3MiOiJ3eGNvbXBvbmVudCIsInN1YiI6IlVzZXIgVG9rZW4iLCJleHAiOjE3NzM5MzIyNTQsIm5iZiI6MTc3Mzg4OTA1NCwiaWF0IjoxNzczODg5MDU0LCJqdGkiOiIxIn0.pXn9eTsUp2vdP_vhoAc2XTilFYtf3K_e-2v9w5X22us";

    // Excel输出路径
    private static final String EXCEL_PATH = "wechat_authorizers.xlsx";

    public static void main(String[] args) {
        try {
            // 1. 请求API获取数据
            List<Authorizer> authorizers = fetchDataFromAPI();

            // 2. 导出到Excel
            exportToExcel(authorizers);

            System.out.println("数据导出成功！文件路径: " + EXCEL_PATH);
            System.out.println("共导出 " + authorizers.size() + " 条记录");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Authorizer> fetchDataFromAPI() throws IOException {
        List<Authorizer> allList = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();

        int offset = 0;
        int limit = 20; // 每页100条，可根据API限制调整
        int total = 0;
        boolean hasMore = true;

        try {
            while (hasMore) {
                // 构建分页URL
                String url = API_URL + "?offset=" + offset + "&limit=" + limit + "&appid=";

                HttpGet httpGet = new HttpGet(url);
                httpGet.setHeader("Authorization", AUTHORIZATION);
                httpGet.setHeader("Content-Type", "application/json");

                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity, "UTF-8");

                // 解析JSON
                JSONObject jsonObject = JSON.parseObject(result);

                if (jsonObject.getIntValue("code") != 0) {
                    throw new RuntimeException("API请求失败: " + jsonObject.getString("errorMsg"));
                }

                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray records = data.getJSONArray("records");
                total = data.getIntValue("total");

                System.out.println("正在获取: offset=" + offset + ", 本页" + records.size() + "条, 总计" + total + "条");

                // 解析当前页数据
                for (int i = 0; i < records.size(); i++) {
                    JSONObject item = records.getJSONObject(i);
                    Authorizer authorizer = parseAuthorizer(item);
                    allList.add(authorizer);
                }

                // 判断是否还有下一页
                offset += records.size();
                hasMore = offset < total && records.size() > 0;

                // 防止死循环，如果返回0条但total还没达到，也退出
                if (records.size() == 0) {
                    break;
                }
            }

            System.out.println("数据获取完成，共 " + allList.size() + " 条");

        } finally {
            httpClient.close();
        }

        return allList;
    }

    /**
     * 解析单个Authorizer对象
     */
    private static Authorizer parseAuthorizer(JSONObject item) {
        Authorizer authorizer = new Authorizer();
        authorizer.setId(item.getIntValue("id"));
        authorizer.setAppid(item.getString("appid"));
        authorizer.setAppType(item.getIntValue("appType"));
        authorizer.setServiceType(item.getIntValue("serviceType"));
        authorizer.setNickName(item.getString("nickName"));
        authorizer.setUserName(item.getString("userName"));
        authorizer.setHeadImg(item.getString("headImg"));
        authorizer.setQrcodeUrl(item.getString("qrcodeUrl"));
        authorizer.setPrincipalName(item.getString("principalName"));
        authorizer.setRefreshToken(item.getString("refreshToken"));
        authorizer.setFuncInfo(item.getString("funcInfo"));
        authorizer.setVerifyInfo(item.getIntValue("verifyInfo"));
        authorizer.setScene(item.getString("scene"));
        authorizer.setRemark(item.getString("remark"));
        authorizer.setAuthTime(item.getString("authTime"));
        authorizer.setRegisterType(item.getIntValue("registerType"));
        authorizer.setAccountStatus(item.getIntValue("accountStatus"));
        return authorizer;
    }
    /**
     * 导出数据到Excel
     */
    private static void exportToExcel(List<Authorizer> authorizers) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("授权公众号列表");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "ID", "AppID", "账号类型", "服务类型", "昵称", "原始ID",
                "主体名称", "授权时间", "认证状态", "账号状态", "功能权限", "备注"
        };

        // 设置标题样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowNum = 1;
        for (Authorizer auth : authorizers) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(auth.getId());
            row.createCell(1).setCellValue(auth.getAppid());
            row.createCell(2).setCellValue(getAppTypeName(auth.getAppType()));
            row.createCell(3).setCellValue(getServiceTypeName(auth.getServiceType()));
            row.createCell(4).setCellValue(auth.getNickName());
            row.createCell(5).setCellValue(auth.getUserName());
            row.createCell(6).setCellValue(auth.getPrincipalName());
            row.createCell(7).setCellValue(auth.getAuthTime());
            row.createCell(8).setCellValue(getVerifyInfoName(auth.getVerifyInfo()));
            row.createCell(9).setCellValue(getAccountStatusName(auth.getAccountStatus()));
            row.createCell(10).setCellValue(auth.getFuncInfo());
            row.createCell(11).setCellValue(auth.getRemark());
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入文件
        try (FileOutputStream fileOut = new FileOutputStream(EXCEL_PATH)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }

    // ============ 辅助方法 ============

    private static String getAppTypeName(int type) {
        switch (type) {
            case 1: return "公众号";
            case 2: return "小程序";
            case 3: return "公众号+小程序";
            case 6: return "视频号";
            default: return "未知(" + type + ")";
        }
    }

    private static String getServiceTypeName(int type) {
        switch (type) {
            case 0: return "无";
            case 1: return "订阅号";
            case 2: return "服务号";
            default: return "未知(" + type + ")";
        }
    }

    private static String getVerifyInfoName(int info) {
        switch (info) {
            case -1: return "未认证";
            case 0: return "微信认证";
            case 1: return "新浪微博认证";
            case 2: return "腾讯微博认证";
            case 3: return "资质认证";
            case 4: return "微信认证+资质认证";
            default: return "未知(" + info + ")";
        }
    }

    private static String getAccountStatusName(int status) {
        return status == 1 ? "正常" : "异常(" + status + ")";
    }

    // ============ 数据模型 ============

    public static class Authorizer {
        private int id;
        private String appid;
        private int appType;
        private int serviceType;
        private String nickName;
        private String userName;
        private String headImg;
        private String qrcodeUrl;
        private String principalName;
        private String refreshToken;
        private String funcInfo;
        private int verifyInfo;
        private String scene;
        private String remark;
        private String authTime;
        private int registerType;
        private int accountStatus;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getAppid() { return appid; }
        public void setAppid(String appid) { this.appid = appid; }

        public int getAppType() { return appType; }
        public void setAppType(int appType) { this.appType = appType; }

        public int getServiceType() { return serviceType; }
        public void setServiceType(int serviceType) { this.serviceType = serviceType; }

        public String getNickName() { return nickName; }
        public void setNickName(String nickName) { this.nickName = nickName; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getHeadImg() { return headImg; }
        public void setHeadImg(String headImg) { this.headImg = headImg; }

        public String getQrcodeUrl() { return qrcodeUrl; }
        public void setQrcodeUrl(String qrcodeUrl) { this.qrcodeUrl = qrcodeUrl; }

        public String getPrincipalName() { return principalName; }
        public void setPrincipalName(String principalName) { this.principalName = principalName; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

        public String getFuncInfo() { return funcInfo; }
        public void setFuncInfo(String funcInfo) { this.funcInfo = funcInfo; }

        public int getVerifyInfo() { return verifyInfo; }
        public void setVerifyInfo(int verifyInfo) { this.verifyInfo = verifyInfo; }

        public String getScene() { return scene; }
        public void setScene(String scene) { this.scene = scene; }

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }

        public String getAuthTime() { return authTime; }
        public void setAuthTime(String authTime) { this.authTime = authTime; }

        public int getRegisterType() { return registerType; }
        public void setRegisterType(int registerType) { this.registerType = registerType; }

        public int getAccountStatus() { return accountStatus; }
        public void setAccountStatus(int accountStatus) { this.accountStatus = accountStatus; }
    }
}
