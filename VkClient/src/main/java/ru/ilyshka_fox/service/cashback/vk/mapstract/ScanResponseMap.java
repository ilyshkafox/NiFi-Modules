package ru.ilyshka_fox.service.cashback.vk.mapstract;

import org.mapstruct.Mapper;
import ru.ilyshka_fox.service.cashback.vk.api.dto.ScanResponseItem;
import ru.ilyshka_fox.service.cashback.vk.dto.ScanResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScanResponseMap {

    default List<ScanResponseItem> map(ScanResponse source) {
        return map(source.getResponse().getItems().getData());
    }

    List<ScanResponseItem> map(List<ScanResponse.DataItem> source);

    ScanResponseItem map(ScanResponse.DataItem source);
}
