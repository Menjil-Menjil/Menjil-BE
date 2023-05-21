package seoultech.capstone.menjil;

import lombok.Builder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        System.out.println("hello 요청 들어옴");
        return "http://menjil-menjil.com:8081 의 GET 요청이 잘 들어왔습니다.";
    }

    @PostMapping("/hello2")
    public Test2 test(@RequestBody Hello hello) {
        return Test2.builder()
                .a(hello.a)
                .b("POST 요청이 잘 들어왔습니다")
                .build();
    }


    static class Test2 {
        public int a;
        public String b;

        @Builder
        public Test2(int a, String b) {
            this.a = a;
            this.b = b;
        }
    }
}
