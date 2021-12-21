package by.vstu.electronicjournal.service.utils.impl;

import by.vstu.electronicjournal.dto.JournalHeaderDTO;
import by.vstu.electronicjournal.dto.JournalSiteDTO;
import by.vstu.electronicjournal.dto.requestBodyParams.ContentDTO;
import by.vstu.electronicjournal.dto.requestBodyParams.ParamsForCreateJournalHeader;
import by.vstu.electronicjournal.service.JournalContentService;
import by.vstu.electronicjournal.service.JournalHeaderService;
import by.vstu.electronicjournal.service.JournalSiteService;
import by.vstu.electronicjournal.service.TypeClassService;
import by.vstu.electronicjournal.service.utils.UtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

import static java.time.LocalDate.now;

@Service
@Transactional
@EnableScheduling
public class UtilServiceImpl implements UtilService {

    @Value("${entrance.timetable}")
    private String path;

    @Autowired
    private JournalSiteService journalSiteService;

    @Autowired
    private JournalHeaderService journalHeaderService;

    @Autowired
    private JournalContentService journalContentService;

    @Autowired
    private TypeClassService typeClassService;


    @Override
    public void generate() {
        journalContentService.generate(journalHeaderService.generate(journalSiteService.generate()));
    }

    @Scheduled(cron = "0 0 7 * * *")
    @Override
    public void generateJournalHeadersEveryDay() {

        System.out.println(LocalTime.now());

        for (ContentDTO dto : getContentFromTimetable()) {

            List<JournalSiteDTO> siteDTOS = journalSiteService.search(
                    String.format("discipline.name==\'%s\';teacher.surname==%s;teacher.name==%s*;teacher.patronymic==%s*;group.name==\'%s\'",
                            dto.getDisciplineName(),
                            dto.getTeacherFio().split(" ")[0],
                            dto.getTeacherFio().split(" ")[1],
                            dto.getTeacherFio().split(" ")[2],
                            dto.getGroupName()
                    )
            );
            for (JournalSiteDTO journalSiteDTO : siteDTOS) {

                boolean flag = false;

                for (JournalHeaderDTO journalHeaderDTO : journalSiteDTO.getJournalHeaders()) {
                    try {
                        if (journalHeaderDTO.getDateOfLesson().isEqual(now())) {
                            flag = true;
                        }
                    } catch (NullPointerException e) {
                        continue;
                    }

                }

                if (flag) continue;

                ParamsForCreateJournalHeader params = new ParamsForCreateJournalHeader();
                JournalHeaderDTO journalHeaderDTO = new JournalHeaderDTO();

                journalHeaderDTO.setSubGroup(dto.getSubGroup());
                journalHeaderDTO.setDateOfLesson(dto.getLessonDate());
                journalHeaderDTO.setTypeClass(typeClassService.validator("name==\'" + dto.getTypeClassName() + "\'").get(0));

                params.setJournalSiteId(journalSiteDTO.getId());
                params.setJournalHeaderDTO(journalHeaderDTO);

                journalHeaderService.create(params);
            }
        }
    }

    private LinkedList<ContentDTO> getContentFromTimetable() {
        RestTemplate restTemplate = new RestTemplate();

        String query = String.format("%s/content/search?q=lessonDate==%s",
                path,
                now()
        );
        LinkedList<ContentDTO> contentDTOS =
                restTemplate.exchange(query, HttpMethod.GET, null, new ParameterizedTypeReference<LinkedList<ContentDTO>>() {
                }).getBody();

        for (ContentDTO dto : contentDTOS) {
            if (dto.getChanges() != null) {
                if (!dto.getChanges().getPostponed().isEqual(now())) {
                    contentDTOS.remove(dto);
                    continue;
                }
                if (dto.getChanges().getCanceled() != null && dto.getChanges().getPostponed().isEqual(null)) {
                    contentDTOS.remove(dto);
                    continue;
                }
            }
        }

        query = String.format("%s/content/search?q=changes.postponed==%s",
                path,
                now()
        );

        LinkedList<ContentDTO> postponedContentDTO =
                restTemplate.exchange(query, HttpMethod.GET, null, new ParameterizedTypeReference<LinkedList<ContentDTO>>() {
                }).getBody();

        contentDTOS.addAll(postponedContentDTO);

        return contentDTOS;
    }
}

