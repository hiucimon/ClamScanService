package hello;

import hello.storage.CommandLine;
import hello.storage.StorageFileNotFoundException;
import hello.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

//        model.addAttribute("files", storageService
//                .loadAll()
//                .map(path ->
//                        MvcUriComponentsBuilder
//                                .fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
//                                .build().toString())
//                .collect(Collectors.toList()));

        return "uploadForm";
    }

//    @GetMapping("/files/{filename:.+}")
//    @ResponseBody
//    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//
//        Resource file = storageService.loadAsResource(filename);
//        return ResponseEntity
//                .ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
//                .body(file);
//    }

    @PostMapping("/")
    public @ResponseBody String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        //storageService.store(file);
        File tempFile;
        final HashMap<String,String> results=new HashMap<String,String>();
        StringBuilder out=new StringBuilder();
        try {
            tempFile = File.createTempFile(file.getName(), ".tmp");
            FileOutputStream OutFile = new FileOutputStream(tempFile);
            OutFile.write(file.getBytes());
            OutFile.close();
            CommandLine c=new CommandLine();
            int r=c.RunCommandWithResults("clamscan -v "+tempFile.getCanonicalFile());

            c.stdout.getLines().forEach(
                    l->{
                        String[] parts = l.split("\\s*:\\s*");
                        if (parts.length<2) {
                            //skip it
                            //System.out.println("vvvvvv---Do nothing to this line");
                        } else if (parts[1].equals("OK")) {
                            //System.out.println("------OK");
                        } else {
                            parts[0]='"'+parts[0].replace(' ','_')+'"';
                            String[] part2 = parts[1].split(" ");
                            if (part2.length>1) {
                                Double w1;
                                float w2;
                                switch (part2[1].toLowerCase()) {
                                    case "kb":  w1=Double.parseDouble(part2[0]);
                                        results.put(parts[0],String.format("%f",w1*1000.0));
                                        break;
                                    case "mb":w1=Double.parseDouble(part2[0]);
                                        results.put(parts[0],String.format("%f",w1*1000000.0));
                                        break;
                                    case "gb":w1=Double.parseDouble(part2[0]);
                                        results.put(parts[0],String.format("%f",w1*1000000000.0));
                                        break;
                                    case "sec":w2=Float.parseFloat(part2[0]);
                                        results.put(parts[0],String.format("%f",w2));
                                        break;
                                    default:
                                }
                            } else {
                                results.put(parts[0],parts[1]);
                            }
                        }
                    }
            );
            out.append("{FileName:"+file.getOriginalFilename()+",");
            results.forEach((k,v)->out.append(k+":"+v+","));
            out.append("TimeStamp:"+System.currentTimeMillis()+"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        redirectAttributes.addFlashAttribute("message",
//                "You successfully uploaded " + file.getOriginalFilename() + "!");
        //return "redirect:/";
        return out.toString();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
