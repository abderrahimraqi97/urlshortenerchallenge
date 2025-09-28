package url.shortener.challenge.service.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.springframework.stereotype.Component;
import url.shortener.challenge.service.CodeGenerator;

@Component
public class NanoIdCodeGenerator implements CodeGenerator {
    @Override
    public String generate(int length) {
        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                NanoIdUtils.DEFAULT_ALPHABET, length);
    }
}