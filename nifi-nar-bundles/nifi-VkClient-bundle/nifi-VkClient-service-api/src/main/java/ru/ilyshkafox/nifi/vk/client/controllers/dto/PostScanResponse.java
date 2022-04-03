package ru.ilyshkafox.nifi.vk.client.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostScanResponse {
    private Response response;
    private String responseString;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Long id;
        @JsonProperty("created_at")
        private LocalDateTime createdAt;
        @JsonProperty("qr_sum")
        private String qrSum;
        @JsonProperty("qr_time")
        private String qrTime;
        private Integer status;
        @JsonProperty("updated_at")
        private String updatedAt;
        @JsonProperty("user_id")
        private Long userId;
        @JsonProperty("valid_for_withdrawn")
        private Boolean validForWithdrawn;
        @JsonProperty("vk_app_id")
        private Long vkAppId;
        private User user;

    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        Integer id;
        String ban_reason;
        LocalDate birth_date;
        Integer birth_day;
        Integer birth_month;
        String bl_customer_id;
        String brandlink_user_id;
        LocalDateTime category_new_visit_at;
        Integer checkbits_count;
        String city_id;
        String city_title;
        Boolean confirmed_18;
        String country_id;
        String country_title;
        Boolean favorites_allowed;
        String first_name;
        String last_name;
        String fraud_counter; // ??? Какйо тип данных
        Boolean is_fraud;
        Boolean metro_user;
        String notifications_allowed; // ??? Какой тип данных
        String notifications_permission_asked; // ??? Какой тип данных
        String notify_coupon_toggle_status; // ??? Какой тип данных
        String perek_created_at; // ??? Какой тип данных
        String perek_user; // ??? Какой тип данных
        Boolean pglead_registered;
        String photo_url;
        String ref_from;
        String ref_link;
        Integer sex;
    }
}
