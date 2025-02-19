package com.bolsadeideas.springboot.webflux.app.controller;

import com.bolsadeideas.springboot.webflux.app.model.document.Category;
import com.bolsadeideas.springboot.webflux.app.model.document.Product;
import com.bolsadeideas.springboot.webflux.app.model.service.impl.CategoryService;
import com.bolsadeideas.springboot.webflux.app.model.service.impl.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

//another way to perfom or persist data in session to edit product
//it requires a SessionStatus.setComplete to mark a process has finished
@SessionAttributes("product")
@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Value("${config.upload.path}")
    private String path;

    private Logger log = LoggerFactory.getLogger(ProductController.class);

    @ModelAttribute("categories")
    public Flux<Category> findAllCategories(){
        return categoryService.findAll();
    }

    @GetMapping("/img/{photoName:.+}")
    public Mono<ResponseEntity<Resource>> viewImage(@PathVariable String photoName) throws MalformedURLException {
        Path path = Paths.get(this.path).toAbsolutePath();
        Resource img = new UrlResource(path.toUri());

        return Mono.just(
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(img.getFilename()))
                        .body(img)
        );
    }

    @GetMapping("/ver/{id}")
    public Mono<String> ver(@PathVariable String id, Model model){
        return productService.findById(id)
                .doOnNext(prod -> {
                    model.addAttribute("product", prod);
                    model.addAttribute("title", "Product Detail");
                }).switchIfEmpty(Mono.just(new Product()))
                .flatMap( prod -> {
                    if(prod.getId() == null){
                        return Mono.error(new InterruptedException("No product found with the provided id"));
                    }
                    return Mono.just(prod);
                }).then(Mono.just("ver"))
                .onErrorResume(ex -> Mono.just("redirect:/list-all?error=no+product+found"));
    }

    @GetMapping({"/list-all","/"})
    public Mono<String> listAll(Model model){
        model.addAttribute("productsList", productService.findAllWthNameInUpperCase());
        model.addAttribute("productsTitle", "Lista de Productos");
        return Mono.just("list-all");
    }

    @GetMapping("/form")
    public Mono<String> create(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("title", "Product Form");
        model.addAttribute("txtBtn", "Create");
        return Mono.just("form");
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editv2(@PathVariable String id, Model model){
        return productService.findById(id).doOnNext(p -> {
            log.info("Product " + p.getName() + " Found");
            model.addAttribute("product", p);
            model.addAttribute("title", "Edit Product");
            model.addAttribute("txtBtn", "Edit");
        }).defaultIfEmpty(new Product())
        .flatMap(product -> {
            if(product.getId() == null){
                return Mono.error(new InterruptedException("No product found with the provided id"));
            }
            return Mono.just(product);
        })
        .thenReturn("form")
        .onErrorResume(ex -> Mono.just("redirect:/list-all?error=no+product+found"));
    }

    @GetMapping("/form/{id}")
    public Mono<String> edit(@PathVariable String id, Model model){
        Mono<Product> productMono = productService.findById(id).doOnNext(p -> {
           log.info("Product " + p.getName() + " Found");
        }).defaultIfEmpty(new Product());
        model.addAttribute("product", productMono);
        model.addAttribute("title", "Edit Product");
        model.addAttribute("txtBtn", "Edit");
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> save(@Valid Product product, BindingResult  result, Model model, @RequestPart("file") FilePart imgFile, SessionStatus status){

        if(result.hasErrors()){
            model.addAttribute("title", "Something was wrong Product");
            model.addAttribute("txtBtn", "Guardar");
            return Mono.just("form");
        }

        status.setComplete();

        Mono<Category> category = categoryService.findById(product.getCategory().getId());

        return category.flatMap( cat -> {
            if(!imgFile.filename().isEmpty()){
                product.setImage(UUID.randomUUID().toString().concat("-").concat(imgFile.filename()
                        .replace(" ", "")
                        .replace(":", "")
                        .replace("\\", "")));
            }
            product.setCategory(cat);
            return productService.save(product);
        }).doOnNext(p -> {
            log.info("Category assined: " + p.getCategory().getName() + ", id: " + p.getCategory().getId());
            log.info("Product created: " + p.getName() + ", id: " + p.getId());
        })
        .flatMap(prod -> {
            if(!imgFile.filename().isEmpty()){
                return imgFile.transferTo(new File(this.path.concat(prod.getImage())));
            }
            return Mono.empty();
        }).thenReturn("redirect:/list-all?success=product+saved+with+success");
    }

    @GetMapping("/delete/{id}")
    public Mono<String> delete(@PathVariable String id){
        return productService.findById(id)
                .defaultIfEmpty(new Product())
                .flatMap(p -> {
                    if(p.getId() == null){
                        return Mono.error(new InterruptedException("Product not found"));
                    }
                    return Mono.just(p);
                })
                .flatMap(productService::delete)
                .then(Mono.just("redirect:/list-all?success=product+removed+with+success"))
                .onErrorResume(ex -> Mono.just("redirect:/list-all?error=product+not+found"));
    }

    @GetMapping("/list-all-with-delay")
    public Mono<String> listAllWithDelay(Model model){
        Flux<Product> products = productService.findAllWthDelay();
        model.addAttribute("productsList", new ReactiveDataDriverContextVariable(products, 1));
        model.addAttribute("productsTitle", "Lista de Productos");
        return Mono.just("list-all");
    }

    @GetMapping("/list-full")
    public Mono<String> listAllFull(Model model){
        model.addAttribute("productsList", productService.findAllRepeat());
        model.addAttribute("productsTitle", "Lista de Productos");
        return Mono.just("list-all");
    }
}
