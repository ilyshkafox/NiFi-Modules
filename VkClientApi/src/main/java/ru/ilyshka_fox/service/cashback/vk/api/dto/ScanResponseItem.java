package ru.ilyshka_fox.service.cashback.vk.api.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor

public class ScanResponseItem {
    private Long id;
    private Long userId;
    private Long status;
    private Long rejectReason;
    private String qrTime;
    private String qrSum;
    private Long createdAt;
    private Boolean withdrawn;
    private String perekBonus; // ХЗ Что тут
    private String freebieActionId; // ХЗ Что тут
    private String brandlinkResponse;
    private String freebiePushSent; // ХЗ Что тут
    private Long vkAppId;
    private Long chooseActionsCount;
    private List<Charges> charges; // Бонусы в рублях у чека.
    private CheckbitBonus checkbitBonus; // Бонусы чекбитами у чека.
    private List<Map<String, Object>> checkbitPrizes; // ХЗ что тут
    private List<Position> positions;
    private List<String> actionRejects; // ХЗ что тут

    @Data
    @NoArgsConstructor
    public static class Charges {
        private Long id;
        private Long scanId;
        private Long actionId;
        private String label;
        private BigDecimal sum;
        private String checkStatus;
        private String checkMeta;
        private String confirmStatus;
        private LocalDateTime createdAt;
        private String chargeType;
        private Integer checkbitsBonus;
        private Integer limitReminding;
        private Map<String, Object> action;
    }

    @Data
    @NoArgsConstructor
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
