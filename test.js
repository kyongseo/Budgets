import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
   vus: 20,
  duration: '10s',
};

export default function () {
    const url = 'http://localhost:9091/budgets/recommend?totalAmount=2000000';

  const params = {
     headers: {
         // 로그인 후 발급받은 토큰
       'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjQsInVzZXJuYW1lIjoidGVzdHVzZXIxMiIsInJvbGUiOiJVU0VSIiwiY2F0ZWdvcnkiOiJhY2Nlc3NUb2tlbiIsImlhdCI6MTc3NDI0MzA3NywiZXhwIjoxNzc0MjQ0ODc3fQ.o3t5GlTrcaxeHdrh9XmCsBxMLLXleyzwml-5e9VIAknYz_Mpdt6C-ghrt4rguLKI_TkvmuRyu2x_0WHHn1fE7A',
       'Content-Type': 'application/json',
      },
   };

   const res = http.get(url, params);

  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(0.5);
}