/*
 * MIT License
 * 
 * Copyright (c) 2026 - Jeff Oliveira
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.erp.gestaofuncionarios.util;

import com.erp.gestaofuncionarios.dao.*;
import com.erp.gestaofuncionarios.model.*;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DataSeeder {

        public static void seed() {
                UserAccountDao userDao = new UserAccountDao();

                String defaultAdminUser = null;
                String defaultAdminPass = null;

                Properties props = new Properties();
                try (java.io.InputStream is = DataSeeder.class.getClassLoader()
                                .getResourceAsStream("database.properties")) {
                        if (is != null) {
                                props.load(is);
                                defaultAdminUser = props.getProperty("default.admin.username");
                                defaultAdminPass = props.getProperty("default.admin.password");
                        } else {
                                throw new IllegalStateException(
                                                "Arquivo database.properties não foi encontrado no classpath.");
                        }
                } catch (java.io.IOException e) {
                        throw new RuntimeException("Erro ao ler o arquivo database.properties", e);
                }

                if (defaultAdminUser == null || defaultAdminUser.isBlank()) {
                        throw new IllegalStateException(
                                        "A propriedade 'default.admin.username' é obrigatória e deve ser definida no arquivo database.properties.");
                }
                if (defaultAdminPass == null || defaultAdminPass.isBlank()) {
                        throw new IllegalStateException(
                                        "A propriedade 'default.admin.password' é obrigatória e deve ser definida no arquivo database.properties.");
                }

                defaultAdminUser = defaultAdminUser.trim();
                defaultAdminPass = defaultAdminPass.trim();

                if (userDao.findByUsername(defaultAdminUser) == null) {
                        UserAccount admin = new UserAccount(defaultAdminUser,
                                        SecurityUtil.hashPassword(defaultAdminPass),
                                        "Administrador do Sistema", "Administrador");
                        userDao.save(admin);
                }

                EmployeeDao empDao = new EmployeeDao();
                List<Employee> existingEmployees;
                try {
                        existingEmployees = empDao.findAll();
                } catch (Exception e) {
                        existingEmployees = List.of();
                }

                if (existingEmployees.size() > 0) {
                        return;
                }

                dropConstraintsSafely();
                clearDatabase();

                DepartmentDao depDao = new DepartmentDao();
                Faker faker = new Faker(Locale.of("pt", "BR"));
                Random rand = new Random();

                Department ti = depDao.save(new Department("TI", "Tecnologia da Informação",
                                "Desenvolvimento, Arquitetura e Infraestrutura"));
                Department rh = depDao.save(new Department("RH", "Recursos Humanos",
                                "Gestão de Talentos, Clima e Desenvolvimento Humano"));
                Department fin = depDao
                                .save(new Department("FIN", "Financeiro", "Contabilidade, Tesouraria e Faturamento"));
                Department mkt = depDao
                                .save(new Department("MKT", "Marketing", "Branding, Mídia Social e Comunicação"));
                Department ops = depDao.save(new Department("OPS", "Operações", "Logística, Qualidade e Processos"));
                Department ven = depDao
                                .save(new Department("VEN", "Vendas", "Atendimento Comercial, Parcerias e Negócios"));
                Department jur = depDao.save(new Department("JUR", "Jurídico", "Compliance, Contratos e Regulatório"));

                Department adm = depDao.save(new Department("ADM", "Administrativo",
                                "Facilities, Facilities e Gestão de Contratos de Consumo"));
                Department log = depDao
                                .save(new Department("LOG", "Logística", "Suprimentos, Armazenagem e Distribuição"));
                Department sac = depDao.save(new Department("SAC", "Atendimento e Suporte",
                                "Sucesso do Cliente, Ouvidoria e Helpdesk"));

                List<Department> depts = List.of(ti, rh, fin, mkt, ops, ven, jur, adm, log, sac);

                PositionDao posDao = new PositionDao();

                // TI
                Position devJr = posDao.save(new Position("Desenvolvedor Júnior",
                                "Desenvolvedor Backend/Frontend Júnior", new BigDecimal("4500.00")));
                Position devPl = posDao.save(new Position("Desenvolvedor Pleno", "Desenvolvedor Full Stack Pleno",
                                new BigDecimal("8000.00")));
                Position devSr = posDao.save(new Position("Desenvolvedor Sênior",
                                "Líder Técnico e Arquiteto de Software", new BigDecimal("14500.00")));
                Position gerTi = posDao.save(new Position("Gerente de TI", "Gestão da equipe de tecnologia",
                                new BigDecimal("18500.00")));

                // RH
                Position analRh = posDao.save(new Position("Analista de RH", "Recrutador e Gestor de Clima",
                                new BigDecimal("4800.00")));
                Position coordRh = posDao.save(new Position("Coordenador de RH", "Líder de DP, Treinamento e Seleção",
                                new BigDecimal("8500.00")));

                // Financeiro
                Position analFin = posDao.save(new Position("Analista Financeiro", "Análise de Contas e Relatórios",
                                new BigDecimal("5500.00")));
                Position controller = posDao.save(new Position("Controller Financeiro",
                                "Auditoria e Planejamento Tributário", new BigDecimal("12000.00")));

                // Marketing
                Position analMkt = posDao.save(new Position("Analista de Marketing", "Especialista em Redes e Tráfego",
                                new BigDecimal("4800.00")));
                Position designer = posDao.save(new Position("Designer Gráfico", "Designer Digital e UI/UX",
                                new BigDecimal("5200.00")));

                // Operações
                Position auxOps = posDao.save(new Position("Auxiliar Operacional", "Apoio logístico e almoxarifado",
                                new BigDecimal("2500.00")));
                Position supOps = posDao.save(new Position("Supervisor de Operações", "Gestão de Qualidade e Processos",
                                new BigDecimal("6500.00")));

                // Vendas
                Position ExecVendas = posDao.save(new Position("Executivo de Vendas", "Vendedor Corporativo B2B",
                                new BigDecimal("5000.00")));
                Position dirComercial = posDao.save(new Position("Diretor Comercial", "Gestão de Metas e Alianças",
                                new BigDecimal("19000.00")));

                // Jurídico
                Position advogado = posDao.save(new Position("Advogado Corporativo", "Compliance e Análise Contratual",
                                new BigDecimal("9500.00")));

                // Administrativo
                Position auxAdm = posDao.save(new Position("Auxiliar Administrativo",
                                "Rotinas de escritório e recepção", new BigDecimal("2200.00")));
                Position analAdm = posDao.save(new Position("Analista Administrativo",
                                "Gestão de contratos e compras internas", new BigDecimal("4600.00")));

                // Logística
                Position analLog = posDao.save(new Position("Analista de Logística",
                                "Planejamento de rotas e suprimentos", new BigDecimal("5000.00")));
                Position coordLog = posDao.save(new Position("Coordenador de Logística",
                                "Gestão do fluxo de cadeia de suprimentos", new BigDecimal("9000.00")));

                // SAC
                Position analSac = posDao.save(new Position("Analista de Suporte",
                                "Atendimento e resolução de incidentes", new BigDecimal("3200.00")));
                Position analCs = posDao.save(new Position("Analista de Customer Success",
                                "Retenção e engajamento do cliente", new BigDecimal("4500.00")));

                BenefitDao benDao = new BenefitDao();
                Benefit vt = benDao.save(new Benefit("Vale Transporte", "Apoio de deslocamento coletivo",
                                new BigDecimal("220.00")));
                Benefit vr = benDao.save(new Benefit("Vale Refeição", "Alimentação diária empresarial",
                                new BigDecimal("550.00")));
                Benefit ps = benDao.save(new Benefit("Plano de Saúde", "Plano médico familiar coparticipativo",
                                new BigDecimal("380.00")));
                Benefit od = benDao.save(new Benefit("Plano Odontológico", "Plano odontológico básico",
                                new BigDecimal("85.00")));
                Benefit gym = benDao.save(new Benefit("Gympass", "Subvenção para academias e esportes",
                                new BigDecimal("120.00")));

                List<Benefit> benefitsList = List.of(vt, vr, ps, od, gym);

                EmployeeBenefitDao ebDao = new EmployeeBenefitDao();
                VacationDao vacDao = new VacationDao();
                TrDhoDao trDao = new TrDhoDao();
                TimeRecordDao timeDao = new TimeRecordDao();
                AbsenceDao abDao = new AbsenceDao();
                PayrollDao payDao = new PayrollDao();

                Set<String> usedEmails = new HashSet<>();
                Set<String> usedCpfs = new HashSet<>();
                Set<String> usedRgs = new HashSet<>();

                for (int i = 0; i < 35; i++) {
                        String firstName = "";
                        String lastName = "";
                        String email = "";
                        String cpf = "";
                        String rg = "";

                        while (true) {
                                firstName = faker.name().firstName();
                                lastName = faker.name().lastName();
                                String cleanFirst = stripAccents(firstName.toLowerCase().replaceAll("\\s+", ""));
                                String cleanLast = stripAccents(lastName.toLowerCase().replaceAll("\\s+", ""));
                                email = cleanFirst + "." + cleanLast + "@empresa.com.br";
                                if (!usedEmails.contains(email)) {
                                        usedEmails.add(email);
                                        break;
                                }
                        }

                        while (true) {
                                cpf = String.format("%09d", rand.nextInt(1000000000)) + "99";
                                rg = String.format("%09d", rand.nextInt(1000000000));
                                if (!usedCpfs.contains(cpf) && !usedRgs.contains(rg)) {
                                        usedCpfs.add(cpf);
                                        usedRgs.add(rg);
                                        break;
                                }
                        }

                        String phone = faker.phoneNumber().cellPhone();

                        Date hireDateRaw = faker.date().past(2190, TimeUnit.DAYS);
                        LocalDate hireDate = hireDateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                        Date birthDateRaw = faker.date().birthday(20, 50);
                        LocalDate birthDate = birthDateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                        Gender gender = rand.nextBoolean() ? Gender.MASCULINE : Gender.FEMININE;
                        MaritalStatus marital = MaritalStatus.values()[rand.nextInt(MaritalStatus.values().length)];

                        Department dept = depts.get(rand.nextInt(depts.size()));
                        Position pos = devPl;

                        if ("TI".equals(dept.getCode())) {
                                double r = rand.nextDouble();
                                pos = r < 0.4 ? devJr : (r < 0.8 ? devPl : (r < 0.95 ? devSr : gerTi));
                        } else if ("RH".equals(dept.getCode())) {
                                pos = rand.nextDouble() < 0.7 ? analRh : coordRh;
                        } else if ("FIN".equals(dept.getCode())) {
                                pos = rand.nextDouble() < 0.7 ? analFin : controller;
                        } else if ("MKT".equals(dept.getCode())) {
                                pos = rand.nextDouble() < 0.6 ? analMkt : designer;
                        } else if ("OPS".equals(dept.getCode())) {
                                pos = rand.nextDouble() < 0.7 ? auxOps : supOps;
                        } else if ("VEN".equals(dept.getCode())) {
                                pos = rand.nextDouble() < 0.85 ? ExecVendas : dirComercial;
                        } else if ("JUR".equals(dept.getCode())) {
                                pos = advogado;
                        } else if ("ADM".equals(dept.getCode())) {
                                pos = rand.nextDouble() < 0.7 ? auxAdm : analAdm;
                        } else if ("LOG".equals(dept.getCode())) {
                                pos = rand.nextDouble() < 0.7 ? analLog : coordLog;
                        } else if ("SAC".equals(dept.getCode())) {
                                pos = rand.nextDouble() < 0.6 ? analSac : analCs;
                        }

                        BigDecimal baseSalary = pos.getBaseSalary();
                        BigDecimal realSalary = baseSalary
                                        .multiply(BigDecimal.valueOf(1.0 + (rand.nextDouble() * 0.15)))
                                        .setScale(2, RoundingMode.HALF_UP);

                        Employee emp = buildEmployee(
                                        firstName, lastName, email, cpf, rg, phone, birthDate, gender, marital,
                                        hireDate, EmployeeStatus.ACTIVE, ContractType.CLT, 44, dept, pos, realSalary,
                                        "Banco Itaú", String.valueOf(1000 + rand.nextInt(9000)),
                                        String.valueOf(10000 + rand.nextInt(90000)), "Corrente",
                                        faker.address().streetName(), faker.address().buildingNumber(),
                                        "Apt " + (10 + rand.nextInt(90)), faker.address().streetAddress(),
                                        faker.address().city(), faker.address().state(), "00000-000");

                        emp.getDocuments().setCpf(true);
                        emp.getDocuments().setRg(true);
                        emp.getDocuments().setAddress(true);
                        emp.getDocuments().setAso(rand.nextBoolean());
                        emp.getDocuments().setWorkCard(rand.nextBoolean());
                        emp.getDocuments().setPhoto(rand.nextBoolean());

                        Employee savedEmp = empDao.save(emp);

                        int numBenefits = 1 + rand.nextInt(3);
                        Set<Benefit> selectedBenefits = new HashSet<>();
                        while (selectedBenefits.size() < numBenefits) {
                                selectedBenefits.add(benefitsList.get(rand.nextInt(benefitsList.size())));
                        }
                        for (Benefit b : selectedBenefits) {
                                ebDao.save(new EmployeeBenefit(savedEmp, b, b.getDefaultValue(),
                                                LocalDate.now().withDayOfMonth(1)));
                        }

                        if (rand.nextBoolean()) {
                                trDao.save(new TrainingRecord(
                                                savedEmp,
                                                faker.job().keySkills() + " Avançado",
                                                "Universidade Corporativa",
                                                LocalDate.now().minusMonths(1 + rand.nextInt(12)),
                                                8 + rand.nextInt(32)));
                        }

                        if (ChronoUnit.DAYS.between(hireDate, LocalDate.now()) > 365 && rand.nextBoolean()) {
                                vacDao.save(new Vacation(
                                                savedEmp,
                                                LocalDate.now().plusMonths(1 + rand.nextInt(6)),
                                                LocalDate.now().plusMonths(1 + rand.nextInt(6)).plusDays(30),
                                                "2024/2025",
                                                VacationStatus.SCHEDULED));
                        }

                        LocalDate today = LocalDate.now();
                        for (int d = 1; d <= 5; d++) {
                                LocalDate date = today.minusDays(d);
                                if (date.getDayOfWeek().getValue() < 6) {
                                        TimeRecord tr = new TimeRecord();
                                        tr.setEmployee(savedEmp);
                                        tr.setDate(date);
                                        tr.setClockIn(LocalTime.of(8, 0).plusMinutes(rand.nextInt(30) - 15));
                                        tr.setClockOut(LocalTime.of(17, 0).plusMinutes(rand.nextInt(45)));
                                        tr.setNotes("Ponto eletrônico regular");
                                        timeDao.save(tr);
                                }
                        }

                        if (rand.nextDouble() < 0.15) {
                                LocalDate absenceDate = today.minusDays(5 + rand.nextInt(20));
                                Absence ab = new Absence();
                                ab.setEmployee(savedEmp);
                                ab.setStartDate(absenceDate);
                                ab.setEndDate(absenceDate.plusDays(rand.nextInt(2)));
                                ab.setReason(rand.nextBoolean() ? "Atestado Médico" : "Falta Não Justificada");
                                ab.setExcused(rand.nextBoolean());
                                ab.setNotes("Justificativa registrada");
                                abDao.save(ab);
                        }

                        String currentPeriod = String.format("%02d/%d", today.getMonthValue(), today.getYear());
                        BigDecimal extraAllowance = BigDecimal.valueOf(rand.nextInt(3) * 150);
                        Payroll p = com.erp.gestaofuncionarios.service.PayrollCalculator.generatePayroll(savedEmp,
                                        currentPeriod, extraAllowance);

                        if (rand.nextDouble() < 0.7) {
                                p.setStatus(PayrollStatus.PAID);
                                p.setPaymentDate(today.minusDays(rand.nextInt(5)));
                        } else {
                                p.setStatus(PayrollStatus.DRAFT);
                        }
                        payDao.save(p);
                }

                System.out.println("[Database] Database initialized successfully!");
        }

        private static void dropConstraintsSafely() {
                jakarta.persistence.EntityManager em = JpaUtil.getEntityManager();
                try {
                        em.getTransaction().begin();
                        em.createNativeQuery("ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_gender_check")
                                        .executeUpdate();
                        em.createNativeQuery("ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_status_check")
                                        .executeUpdate();
                        em.createNativeQuery(
                                        "ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_contract_type_check")
                                        .executeUpdate();
                        em.createNativeQuery(
                                        "ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_marital_status_check")
                                        .executeUpdate();
                        em.createNativeQuery("ALTER TABLE vacations DROP CONSTRAINT IF EXISTS vacations_status_check")
                                        .executeUpdate();
                        em.createNativeQuery("ALTER TABLE payrolls DROP CONSTRAINT IF EXISTS payrolls_status_check")
                                        .executeUpdate();
                        em.getTransaction().commit();
                } catch (Exception ex) {
                        if (em.getTransaction().isActive()) {
                                em.getTransaction().rollback();
                        }
                } finally {
                        em.close();
                }
        }

        private static void clearDatabase() {
                jakarta.persistence.EntityManager em = JpaUtil.getEntityManager();
                try {
                        em.getTransaction().begin();
                        em.createQuery("delete from TrainingRecord").executeUpdate();
                        em.createQuery("delete from Vacation").executeUpdate();
                        em.createQuery("delete from EmployeeBenefit").executeUpdate();
                        em.createQuery("delete from Absence").executeUpdate();
                        em.createQuery("delete from Payroll").executeUpdate();
                        em.createQuery("delete from TimeRecord").executeUpdate();
                        em.createQuery("delete from Employee").executeUpdate();
                        em.createQuery("delete from Position").executeUpdate();
                        em.createQuery("delete from Department").executeUpdate();
                        em.createQuery("delete from Benefit").executeUpdate();
                        em.getTransaction().commit();
                } catch (Exception e) {
                        if (em.getTransaction().isActive())
                                em.getTransaction().rollback();
                        System.err.println("[Database] Error clearing tables: " + e.getMessage());
                } finally {
                        em.close();
                }
        }

        private static Employee buildEmployee(
                        String firstName, String lastName, String email, String cpf, String rg,
                        String mobile, LocalDate birthDate, Gender gender, MaritalStatus marital,
                        LocalDate hireDate, EmployeeStatus status, ContractType contract, int hours,
                        Department dept, Position pos, BigDecimal salary,
                        String bank, String agency, String account, String accountType,
                        String street, String number, String complement, String neighborhood,
                        String city, String state, String zip) {

                Employee e = new Employee();
                e.setFirstName(firstName);
                e.setLastName(lastName);
                e.setEmail(email);
                e.setCpf(cpf);
                e.setRg(rg);
                e.setMobilePhone(mobile);
                e.setBirthDate(birthDate);
                e.setGender(gender);
                e.setMaritalStatus(marital);
                e.setNationality("Brasileiro(a)");
                e.setHireDate(hireDate);
                e.setStatus(status);
                e.setContractType(contract);
                e.setWeeklyHours(hours);
                e.setDepartment(dept);
                e.setPosition(pos);
                e.setSalary(salary);

                BankInfo bi = e.getBankInfo();
                bi.setBankName(bank);
                bi.setAgency(agency);
                bi.setAccount(account);
                bi.setAccountType(accountType);

                Address addr = e.getAddress();
                addr.setStreet(street);
                addr.setNumber(number);
                addr.setComplement(complement);
                addr.setNeighborhood(neighborhood);
                addr.setCity(city);
                addr.setState(state);
                addr.setZipCode(zip);

                return e;
        }

        private static String stripAccents(String s) {
                s = Normalizer.normalize(s, Normalizer.Form.NFD);
                s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                return s;
        }
}

class TrDhoDao extends GenericDao<TrainingRecord> {
        public TrDhoDao() {
                super(TrainingRecord.class);
        }
}