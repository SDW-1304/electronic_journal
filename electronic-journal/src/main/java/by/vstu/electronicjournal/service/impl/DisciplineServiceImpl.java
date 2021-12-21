package by.vstu.electronicjournal.service.impl;

import by.vstu.electronicjournal.dto.DisciplineDTO;
import by.vstu.electronicjournal.entity.Discipline;
import by.vstu.electronicjournal.mapper.Mapper;
import by.vstu.electronicjournal.repository.DisciplineRepository;
import by.vstu.electronicjournal.service.DisciplineService;
import by.vstu.electronicjournal.service.common.impl.CommonCRUDServiceImpl;
import by.vstu.electronicjournal.service.utils.ActuatorFromGeneralResources;
import by.vstu.electronicjournal.service.utils.factory.DisciplineFactory;
import by.vstu.electronicjournal.service.utils.impl.ActuatorFromGeneralResourcesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class DisciplineServiceImpl
        extends CommonCRUDServiceImpl<Discipline, DisciplineDTO, DisciplineRepository>
        implements DisciplineService {

    @Value("${entrance.common-info}")
    private String path;

    @Autowired
    private Mapper<Discipline, DisciplineDTO> mapper;

    @Autowired
    private DisciplineRepository disciplineRepository;

    private ActuatorFromGeneralResources<DisciplineDTO> relatedResources;

    public DisciplineServiceImpl() {
        super(Discipline.class, DisciplineDTO.class);
    }

    @PostConstruct
    void settingUp() {
        relatedResources = new ActuatorFromGeneralResourcesImpl(DisciplineDTO.class, disciplineRepository, new DisciplineFactory(), mapper);
    }

    @Override
    public List<DisciplineDTO> search(String query) {

        if (query.isEmpty()) {
            return findAll();
        }

        return mapper.toDTOs(disciplineRepository.findAll(getSpecifications(query)), DisciplineDTO.class);
    }

    @Override
    public List<DisciplineDTO> validator(String query) {
        String queryToCommonInfo = String.format("%s/disciplines/search?q=%s", path, query);
        return relatedResources.findAndAddThings(queryToCommonInfo);
    }
}
