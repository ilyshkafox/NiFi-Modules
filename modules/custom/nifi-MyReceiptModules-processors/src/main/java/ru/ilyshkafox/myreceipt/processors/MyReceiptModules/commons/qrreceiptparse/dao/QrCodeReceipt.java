package ru.ilyshkafox.myreceipt.processors.MyReceiptModules.commons.qrreceiptparse.dao;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@RequiredArgsConstructor
public final class QrCodeReceipt {
    private final String qrString;
    private final LocalDateTime time;          // t - Время формата yyyyMMdd't'HHmmss или yyyyMMdd't'HHmm
    private final BigDecimal sum;              // s - Сумма формата ₽₽.Коп
    private final Long fiscalNumber;           // fn - 16цифр ФН( номер фискального накопителя)
    private final Long fiscalDocument;         // i - ФД (номер фискального документа)
    private final Long fiscalFeature;          // fp - ФП (фискальный признак)
    private final Short type;                  // n - тип системы налогообложения
    //                                           (0-ОСН,1-УСН Доход,2-УСН Доход-Расход, 3-ЕНВД, 4-ЕСН, 5-Патент)
}
