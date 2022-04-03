package ru.ilyshkafox.nifi.vk.client.controllers.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PostScanRequestTest {

    @Test
    void name() throws JsonProcessingException {
        PostScanRequest of = PostScanRequest.of("1f206601-d3b5-443a-9382-b96506d95776");
        System.out.println(new ObjectMapper().writeValueAsString(of));
    }
}