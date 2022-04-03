package ru.ilyshkafox.nifi.vk.client.controllers;

import ru.ilyshkafox.nifi.vk.client.controllers.dto.PostScanResponse;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.ScanResponse;

import java.util.Iterator;

public interface CheckBackClient {
    Iterator<ScanResponse.DataItem> getScanDataItem();

    ScanResponse getScan(int page);

    PostScanResponse postScan(String qrString);
}
