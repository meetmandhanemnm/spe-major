package com.example.spe_mini.Controller;

import lombok.extern.slf4j.Slf4j;
import com.example.spe_mini.Excel.EmployeeExcelExporter;
import com.example.spe_mini.Models.*;
import com.example.spe_mini.Repo.EmployeeRepo;
import com.example.spe_mini.Services.EmployeeServices;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.spe_mini.Security.services.JwtService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/employee")
public class EmployeeController {
    @Autowired
    private EmployeeServices employeeServices;
    @Autowired
    private EmployeeRepo empRepo;
    @Autowired
    private  JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @PostMapping("/create-employee")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee){
        Employee employee1 = this.employeeServices.createEmployee(employee);
        log.info("Employee added with employee id "+employee1.getE_id()+" and employee name "+employee1.getName());
        return new ResponseEntity<>(employee1, HttpStatus.CREATED);
    }
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllEmployees()
    {
        List<Employee> list=this.employeeServices.getAllEmployees();
        log.info("Sent list of all employees ");
        return new ResponseEntity<>(list,HttpStatus.ACCEPTED);
    }

//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest request)
//    {
//        LoginResponse response=this.employeeServices.login(request);
//        return new ResponseEntity<>(response,HttpStatus.ACCEPTED);
//    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest){
        Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        if(authentication.isAuthenticated()) {
            String token=jwtService.generateToken(authRequest.getEmail());
            Employee emp=this.empRepo.findByEmail(authRequest.getEmail()).orElseThrow();
            LoginResponse loginResponse=new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setRoles(emp.getRoles());
            loginResponse.setName(emp.getName());
            loginResponse.setEmail(emp.getEmail());
            loginResponse.setE_id(emp.getE_id());
            log.info("Login successful....");
            return new ResponseEntity<>(loginResponse,HttpStatus.ACCEPTED);
        }
        else {
            log.info("Login failed...");
            return new ResponseEntity<>("Invalid Username or password", HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/get-empById/{emp_id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable("emp_id") Integer id)
    {
        Employee employee=this.employeeServices.getEmployeeByID(id);
        log.info("Employee returned with id "+employee.getE_id());
        return new ResponseEntity<>(employee,HttpStatus.ACCEPTED);
    }

    @GetMapping("/report-employee/{emp-id}")
    public void generateEmployeeReport(HttpServletResponse response, @PathVariable("emp-id") int emp_id, @RequestParam("year") int year, @RequestParam("month") int month) throws IOException
    {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=report_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Activity1> activity1s = employeeServices.getActivity1s(employeeServices.getEmployeeByID(emp_id), month, year);
        List<Activity2> activity2s = employeeServices.getActivity2s(employeeServices.getEmployeeByID(emp_id), month, year);
        List<Activity3> activity3s = employeeServices.getActivity3s(employeeServices.getEmployeeByID(emp_id), month, year);
        List<Activity4> activity4s = employeeServices.getActivity4s(employeeServices.getEmployeeByID(emp_id), month, year);
        List<Activity5> activity5s = employeeServices.getActivity5s(employeeServices.getEmployeeByID(emp_id), month, year);
        log.info("Report generated for employee with id: "+emp_id+" and name: "+ employeeServices.getEmployeeByID(emp_id).getName());
        EmployeeExcelExporter excelExporter = new EmployeeExcelExporter(activity1s, activity2s, activity3s, activity4s, activity5s);
        excelExporter.export(response);
    }

    @GetMapping("/report-admin")
    public void generateAdminReport(HttpServletResponse response, @RequestParam("emp-ids") List<Integer> emp_ids, @RequestParam("year") int year, @RequestParam("month") int month, @RequestParam("activities") List<Integer> activities) throws IOException
    {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=report_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Activity1> activity1s = new ArrayList<Activity1>();
        List<Activity2> activity2s = new ArrayList<Activity2>();
        List<Activity3> activity3s = new ArrayList<Activity3>();
        List<Activity4> activity4s = new ArrayList<Activity4>();
        List<Activity5> activity5s = new ArrayList<Activity5>();
        log.info("Report generated for admin...");

        for(int emp_id:emp_ids) {

            if(activities.contains(Integer.parseInt("1")))
                activity1s.addAll(employeeServices.getActivity1s(employeeServices.getEmployeeByID(emp_id), month, year));
            if(activities.contains(Integer.parseInt("2")))
                activity2s.addAll(employeeServices.getActivity2s(employeeServices.getEmployeeByID(emp_id), month, year));
            if(activities.contains(Integer.parseInt("3")))
                activity3s.addAll(employeeServices.getActivity3s(employeeServices.getEmployeeByID(emp_id), month, year));
            if(activities.contains(Integer.parseInt("4")))
                activity4s.addAll(employeeServices.getActivity4s(employeeServices.getEmployeeByID(emp_id), month, year));
            if(activities.contains(Integer.parseInt("5")))
                activity5s.addAll(employeeServices.getActivity5s(employeeServices.getEmployeeByID(emp_id), month, year));

        }
        EmployeeExcelExporter excelExporter = new EmployeeExcelExporter(activity1s, activity2s, activity3s, activity4s, activity5s);

        excelExporter.export(response);
    }

}
