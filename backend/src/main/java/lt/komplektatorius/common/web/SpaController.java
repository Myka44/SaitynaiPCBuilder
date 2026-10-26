package lt.komplektatorius.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    // Only browser routes forward to React; API and missing asset URLs keep their own responses.
    @GetMapping({"/", "/login", "/register", "/forbidden", "/catalog", "/catalog/{id}",
            "/configurations/{id}", "/app/configurations", "/app/configurations/{id}/edit",
            "/admin/configurations"})
    public String index() {
        return "forward:/index.html";
    }
}
