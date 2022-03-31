package ru.ilyshkafox.nifi.vk.client.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanResponse {
    private Response response;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Items items;
        @JsonProperty(value = "limit_reached")
        private Boolean limitReached;
        private Boolean welcome;
        @JsonProperty(value = "scan_processing_count")
        private Boolean scanProcessingCount;
        @JsonProperty(value = "has_charges_to_retry")
        private Boolean hasChargesToRetry;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        @JsonProperty(value = "current_page")
        private Integer currentPage;
        private List<DataItem> data;
        @JsonProperty(value = "first_page_url")
        private String firstPageUrl;
        private Integer from;
        @JsonProperty(value = "last_page")
        private Integer lastPage;
        @JsonProperty(value = "last_page_url")
        private String lastPageUrl;
        @JsonProperty(value = "next_page_url")
        private String nextPageUrl;
        private String path;
        @JsonProperty(value = "per_page")
        private Integer perPage;
        @JsonProperty(value = "prev_page_url")
        private String prevPageUrl;
        private Integer to;
        private Integer total;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataItem {
        private Long id;
        @JsonProperty(value = "user_id")
        private Long userId;
        /**
         * 3 - <br>
         */
        private Long status;
        /**
         * 10 - <br>
         */
        @JsonProperty(value = "reject_reason")
        private Long rejectReason;
        @JsonProperty(value = "qr_time")
        private String qrTime;
        @JsonProperty(value = "qr_sum")
        private String qrSum;
        @JsonProperty(value = "created_at")
        private Long createdAt;
        private Boolean withdrawn;
        @JsonProperty(value = "perek_bonus")
        private String perekBonus; // ХЗ Что тут
        @JsonProperty(value = "freebie_action_id")
        private String freebieActionId; // ХЗ Что тут
        @JsonProperty(value = "brandlink_response")
        private String brandlinkResponse;
        @JsonProperty(value = "freebie_push_sent")
        private String freebiePushSent; // ХЗ Что тут
        @JsonProperty(value = "vk_app_id")
        private Long vkAppId;
        @JsonProperty(value = "choose_actions_count")
        private Long chooseActionsCount;
        private List<Charges> charges; // Бонусы в рублях у чека.
        @JsonProperty(value = "checkbit_bonus")
        private CheckbitBonus checkbitBonus; // Бонусы чекбитами у чека.
        @JsonProperty(value = "checkbit_prizes")
        private List<Map<String, Object>> checkbitPrizes; // ХЗ что тут
        private List<Position> positions;
        @JsonProperty(value = "action_rejects")
        private List<String> actionRejects; // ХЗ что тут
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Charges {
        private Long id;
        private Long scanId;
        private Long actionId;
        private String label;
        /**
         * Сумма в рублях
         */
        private BigDecimal sum;
        private String checkStatus;
        private String checkMeta;
        /**
         * PAID
         */
        private String confirmStatus;
        private LocalDateTime createdAt;
        /**
         * vk_pay
         */
        private String chargeType;
        private Integer checkbitsBonus;
        private Integer limitReminding;
        private Map<String, Object> action;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CheckbitBonus {
        private Long id;
        private String category;
        private Long amount;
        private Long userId;
        private Long scanId;
        private Long actionId;
        private Long friendId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String label;
    }


    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Position {
        private Long id;
        private Long scanId;
        private Long userId;
        private Long actionId;
        private String name;
        private Long price;
        private Integer quantity;
        private Long sum;
        private LocalDateTime createdAt;
        private List<Long> actionIds;
        private Boolean handed;
        private List<Long> handedActionIds;
        private LocalDateTime scanCreatedAt;
        private Boolean approvedPushSent;
        private Integer position;
        private List<Long> productIds;
        private Boolean canDelete;
        private Long productId;
    }
}
