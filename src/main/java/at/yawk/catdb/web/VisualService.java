package at.yawk.catdb.web;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author yawkat
 */
@Controller
public class VisualService {
    @Autowired Database database;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public void index(HttpServletResponse response) throws IOException {
        response.sendRedirect("/images.html");
    }

    @RequestMapping(value = "/images.html", method = RequestMethod.GET)
    public ModelAndView query(@RequestParam(value = "tag", required = false) Set<String> tags) {
        if (tags == null) { tags = Collections.emptySet(); }

        Collection<Image> images = database.listImages(tags);
        return new ModelAndView("images")
                .addObject("tags", tags)
                .addObject("cats", images.stream()
                        .sorted(Comparator.comparingInt(Image::getId))
                        .collect(Collectors.toList()));
    }
}
