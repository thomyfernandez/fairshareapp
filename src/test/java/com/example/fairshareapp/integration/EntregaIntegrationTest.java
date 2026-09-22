package com.example.fairshareapp.integration;

import com.example.fairshareapp.repository.*;
import com.example.fairshareapp.model.enums.RolUsuario;
import com.example.fairshareapp.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.*;
import java.util.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/** HTTP real de Spring Security + controllers + servicios + H2; sin mocks de negocio. */
@SpringBootTest
class EntregaIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired GastoRepository gastos;
    @Autowired PasswordEncoder encoder;
    MockMvc mvc;
    @BeforeEach void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }
    record Cuenta(long id, String email, String token) {}
    JsonNode call(String method, String path, String token, Object body, int expected) throws Exception {
        var request = MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method), path);
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.contentType("application/json").content(mapper.writeValueAsString(body));
        var response = mvc.perform(request).andReturn().getResponse();
        assertEquals(expected, response.getStatus(), method + " " + path + ": " + response.getContentAsString());
        return response.getContentAsString().isEmpty() ? mapper.createObjectNode() : mapper.readTree(response.getContentAsString());
    }
    Cuenta cuenta() throws Exception {
        String email = UUID.randomUUID()+"@example.com";
        var r = call("POST", "/api/v1/usuarios/registro", null,
            Map.of("nombre","Ana","apellido","Prueba","usuario","ana","email",email,"contra","Password123!","rol","ADMIN"),201);
        assertEquals("USUARIO", r.path("rol").asText()); assertFalse(r.has("contra")); assertFalse(r.has("password"));
        assertTrue(encoder.matches("Password123!", usuarios.findById(r.path("id").asLong()).orElseThrow().getContra()));
        var login = call("POST", "/api/v1/usuarios/login", null, Map.of("email",email,"contra","Password123!"),200);
        assertEquals("Bearer", login.path("tokenType").asText());
        return new Cuenta(r.path("id").asLong(), email, login.path("token").asText());
    }
    JsonNode espacio(Cuenta c) throws Exception {
        return call("POST", "/api/v1/espacios", c.token(), Map.of("nombre","Espacio "+UUID.randomUUID(),
            "tipo","HOGAR","reglaReparto","CINCUENTA_CINCUENTA","presupuestoBase",1000),201);
    }
    void unir(Cuenta c, JsonNode e) throws Exception {
        call("POST", "/api/v1/espacios/unirse", c.token(), Map.of("usuarioId",c.id(),"codigo",e.path("codigo").asText()),201);
    }
    Map<String,Object> gasto(Cuenta pagador, Number monto, Cuenta... participantes) {
        return Map.of("descripcion","Compra compartida","monto",monto,"pagadorId",pagador.id(),"regla","EQUITATIVA",
            "participantes",Arrays.stream(participantes).map(c -> Map.of("usuarioId",c.id())).toList());
    }
    String ruta(JsonNode e, String suffix) { return "/api/v1/espacios/"+e.path("id").asLong()+suffix; }

    @Test void registroLoginRolesYErrores() throws Exception {
        var a=cuenta();
        call("POST","/api/v1/usuarios/registro",null,Map.of("nombre","Ana","apellido","Prueba","usuario","ana",
            "email",a.email().toUpperCase(Locale.ROOT),"contra","Password123!"),409);
        var error=call("POST","/api/v1/usuarios/registro",null,Map.of("email","incorrecto"),400);
        assertTrue(error.has("fieldErrors")); assertEquals(400,error.path("status").asInt());
        call("POST","/api/v1/usuarios/login",null,Map.of("email",a.email(),"contra","Incorrecta123"),401);
        call("GET","/api/v1/usuarios/me",null,null,401);
        call("GET","/api/v1/usuarios/me","token-roto",null,401);
        call("GET","/api/v1/usuarios/me",a.token(),null,200);
        call("GET","/api/v1/usuarios",a.token(),null,403);
        call("GET","/api/usuario/get",a.token(),null,403);
        var u=usuarios.findById(a.id()).orElseThrow();u.setRol(RolUsuario.ADMIN);usuarios.saveAndFlush(u);
        call("GET","/api/v1/usuarios",a.token(),null,200);
    }
    @Test void rechazaTokensVencidosAlteradosYSinExpiracion() throws Exception {
        var a=cuenta();
        byte[] key="test-secret-key-for-unit-and-integration-tests-only-not-for-production".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String expired=io.jsonwebtoken.Jwts.builder().setSubject(a.email()).setExpiration(new Date(1))
            .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(key),io.jsonwebtoken.SignatureAlgorithm.HS256).compact();
        String noExpiry=io.jsonwebtoken.Jwts.builder().setSubject(a.email())
            .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(key),io.jsonwebtoken.SignatureAlgorithm.HS256).compact();
        call("GET","/api/v1/usuarios/me",expired,null,401);
        call("GET","/api/v1/usuarios/me",noExpiry,null,401);
        var wrong=new JwtService("another-test-signing-secret-with-at-least-32-bytes",3600000);
        call("GET","/api/v1/usuarios/me",wrong.generarToken(a.email()),null,401);
    }
    @Test void permisosDerivadosDelTokenYNoDelSolicitante() throws Exception {
        var a=cuenta();var b=cuenta();var outsider=cuenta();var e=espacio(a);unir(b,e);
        call("GET",ruta(e,"/miembros"),outsider.token(),null,403);
        call("PATCH",ruta(e,"/presupuesto-base?presupuestoBase=900&solicitanteId="+a.id()),b.token(),null,403);
        call("PATCH",ruta(e,"/presupuesto-base?presupuestoBase=900"),a.token(),null,200);
        call("POST",ruta(e,"/unirse"),outsider.token(),Map.of("usuarioId",a.id(),"codigo",e.path("codigo").asText()),403);
        call("POST",ruta(e,"/unirse"),b.token(),Map.of("usuarioId",b.id(),"codigo",e.path("codigo").asText()),409);
        call("PATCH",ruta(e,"/miembros/"+a.id()+"/rol?rol=MIEMBRO"),a.token(),null,409);
        assertEquals(0,call("GET","/api/v1/espacios",outsider.token(),null,200).size());
    }
    @Test void sueldosUpsertPropiedadYValidacion() throws Exception {
        var a=cuenta();var b=cuenta();
        var body=Map.of("monto",100000,"tipo","FIJO","frecuencia","MENSUAL","mes",9,"anio",2026);
        var sueldo=call("POST","/api/v1/sueldos",a.token(),body,201);
        long id=sueldo.path("id").asLong();
        call("POST","/api/v1/sueldos",a.token(),body,200);
        assertEquals(1,call("GET","/api/v1/sueldos",a.token(),null,200).size());
        assertEquals(0,call("GET","/api/v1/sueldos",b.token(),null,200).size());
        call("GET","/api/v1/sueldos/"+id,b.token(),null,403);
        call("GET","/api/v1/sueldos?usuarioId="+a.id(),b.token(),null,403);
        var invalid=new HashMap<String,Object>(body);invalid.put("mes",13);
        call("POST","/api/v1/sueldos",a.token(),invalid,400);
        call("DELETE","/api/v1/sueldos/"+id,a.token(),null,204);
        call("GET","/api/v1/sueldos/"+id,a.token(),null,404);
    }
    @Test void gastosValidanMiembrosDuplicadosYRollbackDelLote() throws Exception {
        var a=cuenta();var b=cuenta();var e=espacio(a);
        call("POST",ruta(e,"/gastos"),a.token(),gasto(a,100,a,b),400);
        unir(b,e);
        call("POST",ruta(e,"/gastos"),a.token(),gasto(a,100,a,a),400);
        var invalido=new HashMap<>(gasto(a,100,a,b));invalido.put("pagadorId",999999);
        call("POST",ruta(e,"/gastos/lote"),a.token(),List.of(gasto(a,100,a,b),invalido),404);
        assertEquals(0,call("GET",ruta(e,"/gastos"),a.token(),null,200).size());
        var g=call("POST",ruta(e,"/gastos"),a.token(),gasto(a,0.01,a,b),201);
        java.math.BigDecimal total=java.math.BigDecimal.ZERO;
        for(var p:g.path("participantes")) {var amount=p.path("importe").decimalValue();assertTrue(amount.signum()>=0);total=total.add(amount);}
        assertEquals(0,total.compareTo(new java.math.BigDecimal("0.01")));
    }
    @Test void pagarYRecalcularNoResucitaDeudasNiPierdeCentavos() throws Exception {
        var a=cuenta();var b=cuenta();var e=espacio(a);unir(b,e);
        call("POST",ruta(e,"/gastos"),a.token(),gasto(a,100,a,b),201);
        var debts=call("GET",ruta(e,"/balance"),a.token(),null,200).path("deudas");
        long id=debts.get(0).path("id").asLong();
        call("POST","/api/v1/deudas/"+id+"/saldar",b.token(),Map.of("monto",10),200);
        for(int i=0;i<2;i++) assertEquals(40,call("GET",ruta(e,"/balance"),a.token(),null,200).path("deudas").get(0).path("monto").asDouble());
        call("POST","/api/v1/deudas/"+id+"/saldar",b.token(),Map.of("monto",40.01),400);
        call("POST","/api/v1/deudas/"+id+"/saldar",b.token(),null,200);
        for(int i=0;i<2;i++) assertEquals(0,call("GET",ruta(e,"/balance"),a.token(),null,200).path("deudas").size());
        call("POST",ruta(e,"/gastos"),a.token(),gasto(a,0.02,a,b),201);
        var cent=call("GET",ruta(e,"/balance"),a.token(),null,200).path("deudas").get(0);
        assertEquals(0.01,cent.path("monto").asDouble());
        call("POST","/api/v1/deudas/"+cent.path("id").asLong()+"/saldar",b.token(),null,200);
        assertEquals(0,call("GET",ruta(e,"/balance"),a.token(),null,200).path("deudas").size());
    }
    @Test void liquidacionValidaTodoElLoteYNoDescuentaAnteErrores() throws Exception {
        var a=cuenta();var b=cuenta();var e=espacio(a);unir(b,e);var otro=espacio(b);
        var g=call("POST",ruta(e,"/gastos"),a.token(),gasto(a,100,a,b),201);
        var ajeno=call("POST",ruta(otro,"/gastos"),b.token(),gasto(b,100,b),201);
        call("POST",ruta(e,"/liquidaciones/cierre"),a.token(),Map.of("gastoIds",List.of(g.path("id").asLong(),ajeno.path("id").asLong())),400);
        assertEquals(1000,call("GET",ruta(e,""),a.token(),null,200).path("presupuestoBase").asDouble());
        call("POST",ruta(e,"/liquidaciones/cierre"),b.token(),null,403);
        call("POST",ruta(e,"/liquidaciones/cierre"),a.token(),Map.of("mes",13),400);
        call("POST",ruta(e,"/liquidaciones/cierre"),a.token(),Map.of("gastoIds",List.of(g.path("id").asLong())),201);
        call("POST",ruta(e,"/liquidaciones/cierre"),a.token(),Map.of("gastoIds",List.of(g.path("id").asLong())),409);
        assertEquals(900,call("GET",ruta(e,""),a.token(),null,200).path("presupuestoBase").asDouble());
        call("DELETE","/api/v1/gastos/"+g.path("id").asLong(),a.token(),null,400);
        call("GET",ruta(e,"/liquidaciones/historial?mes=9"),a.token(),null,400);
    }
    @Test void plantillaVencidaDevuelveContratoComunYSePuedeRenovar() throws Exception {
        var a=cuenta();var b=cuenta();var e=espacio(a);unir(b,e);
        var p=call("POST",ruta(e,"/favoritos"),a.token(),Map.of("nombre","Alquiler","pagadorId",a.id(),"montoBase",100,
            "fechaProximaRevision",LocalDate.now().minusDays(1).toString(),"frecuenciaAjusteMeses",1),201);
        String path="/api/v1/favoritos/"+p.path("id").asLong();
        var error=call("POST",path+"/ejecutar",a.token(),null,409);
        assertEquals("CICLO_VENCIDO",error.path("code").asText());
        call("PUT",path+"/actualizar-monto",a.token(),Map.of("nuevoMontoBase",120),200);
        assertEquals(2,call("POST",path+"/ejecutar",a.token(),null,201).path("participantes").size());
    }
    @Test void corsPermitePatchYFrameworkNoConvierte404En500() throws Exception {
        var res=mvc.perform(MockMvcRequestBuilders.options("/api/v1/espacios/1/presupuesto-base")
            .header("Origin","http://localhost:5173").header("Access-Control-Request-Method","PATCH")
            .header("Access-Control-Request-Headers","authorization")).andReturn().getResponse();
        assertEquals(200,res.getStatus());assertTrue(res.getHeader("Access-Control-Allow-Methods").contains("PATCH"));
        var a=cuenta();call("GET","/api/v1/no-existe",a.token(),null,404);
    }
}
