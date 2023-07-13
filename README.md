# Menjil-Menjil Server



## 1. Tech Stack



### BackEnd

| 역할                 | 종류                                                         |
| -------------------- | ------------------------------------------------------------ |
| Programming Language | <img alt="RED" src ="https://img.shields.io/badge/JAVA-004027.svg?&style=for-the-badge&logo=Jameson&logoColor=white"/> |
| Server Framework     | <img alt="RED" src ="https://img.shields.io/badge/SPRING Boot-6DB33F.svg?&style=for-the-badge&logo=SpringBoot&logoColor=white"/> |
| Database             | <img alt="RED" src ="https://img.shields.io/badge/Mysql-003545.svg?&style=for-the-badge&logo=Mysql&logoColor=white"/>  <img src="https://img.shields.io/badge/mongoDB-47A248?style=for-the-badge&logo=MongoDB&logoColor=white"> |
| Version Control      | ![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white) ![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white) |
| CI/CD                |  <img src="https://img.shields.io/badge/GitHub Action-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">|
| Testing                | <img src="https://img.shields.io/badge/junit5-25A162?style=for-the-badge&logo=junit5&logoColor=white"> |



### Infra

| 역할                 | 종류                                                         |
| -------------------- | ------------------------------------------------------------ |
| Web Application Server   | <img src="https://img.shields.io/badge/amazon ec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"> |
| Database Service     | <img alt="RED" src ="https://img.shields.io/badge/Amazon Rds-527FFF.svg?&style=for-the-badge&logo=AmazonRds&logoColor=white"/> <img src="https://img.shields.io/badge/amazondynamodb-4053D6?style=for-the-badge&logo=amazondynamodb&logoColor=white"> |
| Domain               | AWS Route53 |






## 2. Commit Convention

<aside>
📍  git commit message convention
`ex) feat(변경한 파일) : 변경 내용 (/#issue num)`

```plain
- feat:      새로운 기능 구현
- fix:       버그, 오류 해결
- chore:     src 또는 test 파일을 수정하지 않는 기타 변경 사항 ( 새로운 파일 생성, 파일 이동, 이름 변경 등 )
- refactor:  버그 수정이나 기능 추가가 없는 코드 변경 ( 코드 구조 변경 등의 리팩토링 )
- style:     코드의 의미에 영향을 미치지 않는 변경 사항 ( 코드 형식, 세미콜론 추가: 비즈니스 로직에 변경 없음 )
- build:    빌드 시스템 또는 외부에 영향을 미치는 변경 사항 종속성 ( 라이브러리 추가 등 )
- perf:      성능을 향상 시키기 위한 코드 변경
- test:      테스트 추가 또는 이전 테스트 수정
- docs:      README나 WIKI 등의 문서 개정
- revert:    이전 커밋을 되돌리는 경우
- ci:      CI 구성 파일 및 스크립트 변경
- cd: 		 CD 구성 파일 및 스크립트 변경 
- Merge: 다른 branch를 merge하는 경우
- Init : Initial commit을 하는 경우
```
