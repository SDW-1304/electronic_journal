package by.vstu.electronicjournal.controller;

import by.vstu.electronicjournal.dto.JournalSiteDTO;
import by.vstu.electronicjournal.service.JournalSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("journal-sites")
public class JournalSiteController {

    @Autowired
    private JournalSiteService journalSiteService;

    @GetMapping("search")
    public List<JournalSiteDTO> search(@RequestParam("q") String query) {
        return journalSiteService.search(query);
    }

    @PostMapping
    public JournalSiteDTO create(@RequestBody JournalSiteDTO dto) {
        return journalSiteService.create(dto);
    }

    @GetMapping("{id}")
    public JournalSiteDTO getById(@PathVariable("id") Long id) {
        return journalSiteService.findOne(id);
    }

    @PatchMapping("{id}")
    public JournalSiteDTO editById(@PathVariable("id") Long id, @RequestBody JournalSiteDTO dto) {
        return journalSiteService.update(id, dto);
    }

    @DeleteMapping("{id}")
    public void deleteById(@PathVariable("id") Long id) {
        journalSiteService.deleteById(id);
    }
}
