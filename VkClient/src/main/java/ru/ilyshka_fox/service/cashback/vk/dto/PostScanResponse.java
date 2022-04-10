package ru.ilyshka_fox.service.cashback.vk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        private Integer id;
        private String ban_reason;
        private LocalDate birth_date;
        private Integer birth_day;
        private Integer birth_month;
        private String bl_customer_id;
        private String brandlink_user_id;
        private LocalDateTime category_new_visit_at;
        private Integer checkbits_count;
        private String city_id;
        private String city_title;
        private Boolean confirmed_18;
        private String country_id;
        private String country_title;
        private Boolean favorites_allowed;
        private String first_name;
        private String last_name;
        private String fraud_counter; // ??? Какйо тип данных
        private Boolean is_fraud;
        private Boolean metro_user;
        private String notifications_allowed; // ??? Какой тип данных
        private String notifications_permission_asked; // ??? Какой тип данных
        private String notify_coupon_toggle_status; // ??? Какой тип данных
        private String perek_created_at; // ??? Какой тип данных
        private String perek_user; // ??? Какой тип данных
        private Boolean pglead_registered;
        private String photo_url;
        private String ref_from;
        private String ref_link;
        private Integer sex;
    }
}
