package com.bluepanda22.blockgameresourcepacks;

import dev.bnjc.bglib.BGIData;
import dev.bnjc.bglib.BGIField;
import dev.bnjc.bglib.BGIParseResult;
import dev.bnjc.bglib.BGIParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;

public class Test {

    private static final Logger log = LogManager.getLogger(Test.class);

    public static void main(String[] args) {
        System.out.println("Hello World!");
//        byte[] input = new byte[]{7,0,1,0,1,7,-88,-18,-67,-57,3,16,75,82,79,71,78,65,82,83,95,67,76,69,65,86,69,82};
        byte[] input = new byte[]{7,0,1,0,1,-57,-5,-70,-57,10,3,16,75,82,79,71,78,65,82,83,95,67,76,69,65,86,69,82};
        BGIParseResult<BGIData> data = BGIParser.parse(input);
        data.ifSuccess((item) -> {
            System.out.println(item);
            Optional<String> itemId = item.getString(BGIField.ITEM_ID);
            PrintStream var10001 = System.out;
            Objects.requireNonNull(var10001);
            itemId.ifPresent(var10001::println);
        });
        data.ifError((err) -> {
            System.err.println(err.getErrorCode());
            System.err.println(err.getMessage());
            log.error(err.getCause());
        });
    }

}
