# 433-project

This is a repository for CSED434 project.  
Goal: Implement distributed sorting key/value records stored on multiple machines.  

## Team members

Team color: Blue  
1. Soomin Choi (20160169)
2. Subin Park (20180953)

## How to run
1. Using sbt  
   - master
    ```bash
    sbt "run [workerNum]"
    # e.g.
    sbt "run 2"
    ```
   - worker
    ```bash
    sbt "run [master ip:port] -I [input folder 1] [input folder 2] -O [output folder]"
    # e.g.
    sbt "run 192.168.10.100:8000 -I /input10 -O /home/blue/output"
    ```

2. Using executable file  
  Executable files `master` and `worker` are in the root folder.
   - master
    ```bash
    ./master [workerNum]
    # e.g.
    ./master 2
    ```
   - worker
    ```bash
    ./worker [master ip:port] -I [input folder 1] [input folder 2] -O [output folder]
    # e.g.
    ./worker 192.168.10.100:8000 -I /input10 -O /home/blue/output
    ```

## Result
### 1 worker with input10 (306M)
- master  
  ![image](https://user-images.githubusercontent.com/35210772/145704439-96d2576f-6745-4a5a-b4aa-1b9596c9765d.png)
- worker  
  ![image](https://user-images.githubusercontent.com/35210772/145704456-645b3df7-50a2-419b-ba8c-1ca3d09ced54.png)
  실행 후 output 파일이 잘 생성되었다.
  ![image](https://user-images.githubusercontent.com/35210772/145704498-86c89b04-9fac-488e-bb86-761ac6fe20e5.png)
  All output files are in order and the number or records is same as original.
### 1 worker with input300 (9G)
- worker  
  ![image](https://user-images.githubusercontent.com/35210772/145710941-e2381a1f-18c4-4922-8557-ee5d478c1b2a.png)
  ![image](https://user-images.githubusercontent.com/35210772/145711308-d5ff859e-3a78-477d-ba0a-23e4a7917961.png)
  All output files are in order and the number or records is same as original. 

### 3 workers with input 10 (306M * 3)
- master  
  ![image](https://user-images.githubusercontent.com/35210772/145705719-f8809a6a-ee15-4bf2-aedf-e8b41bba0590.png)
  실행 후 worker를 순서대로 출력한다.
- worker 1  
  ![image](https://user-images.githubusercontent.com/35210772/145705738-c919b8bd-7fa0-4439-acf6-381be4560e61.png)
  ![image](https://user-images.githubusercontent.com/35210772/145705809-7272082f-6c87-4446-9293-7726e2341a1e.png)
- worker 2  
  ![image](https://user-images.githubusercontent.com/35210772/145705745-f0a51b6f-1ed9-466d-8a96-fd01517304ce.png)
  ![image](https://user-images.githubusercontent.com/35210772/145705820-2e726f42-8dd7-409c-a2bd-eb284f28c854.png)
- worker 3  
  ![image](https://user-images.githubusercontent.com/35210772/145705747-7dab3d8a-0986-495c-890e-4a4bd0df9cf9.png)
  ![image](https://user-images.githubusercontent.com/35210772/145705831-a572011d-e6b7-48e4-a6e1-3bad35658a09.png)
- Result summary  
  - All output files in a worker are in order.  
  - Also the order between workers are in order.   
  - The number of records in total is same as original records(306M * 3).
  - 2/3 of records are duplicated, since the input files in a worker is same as others'.

### 3 workers with input 300 (9G * 3)
- worker 1  
  ![image](https://user-images.githubusercontent.com/35210772/145708923-ad78e028-9bc9-435a-b31e-fe56e7b462d6.png)
  ![image](https://user-images.githubusercontent.com/35210772/145708995-588f0d10-1ca8-4618-8023-0e8259ee7ec0.png)
- worker 2  
  ![image](https://user-images.githubusercontent.com/35210772/145708925-4149dcf4-9655-4913-a637-d7180df570f3.png)
  ![image](https://user-images.githubusercontent.com/35210772/145709002-f9ec84d9-9dfb-49f9-8aa8-817b118ee3c8.png)
- worker 3  
  ![image](https://user-images.githubusercontent.com/35210772/145708854-56440fcf-734e-4c4d-a7cd-0710d2f58bf4.png)
  ![image](https://user-images.githubusercontent.com/35210772/145711205-28f7331e-1800-4622-80d9-7a191f881652.png)
- Result summary  
  - All output files in a worker are in order.  
  - Also the order between workers are in order.   
  - The number of records in total is same as original records(306M * 3).
  - 2/3 of records are duplicated, since the input files in a worker is same as others'.

## Plan

Check revised plan with milestones [here](https://www.notion.so/choisium/Milestone-fc4067d43e9749d2ab01968cddea3cfa)


<table>
  <col>
  <colgroup span="2"></colgroup>
  <colgroup span="2"></colgroup>
  <tr>
    <th rowspan="2">Date</th>
    <th rowspan="2">Subject</th>
    <th colspan="2" scope="colgroup">TODO</th>
  </tr>
  <tr>
    <th scope="col">Network(Soomin)</th>
    <th scope="col">Logic(Subin)</th>
  </tr>
  <tr>
    <td scope="row">Nov. 1st week</td>
    <td>Search and Learn Library</td>
    <td colspan="2">
        <ul>
            <li>Messaging library(netty)</li>
            <li>External sorting library</li>
        </ul>
    </td>
  </tr>
  <tr>
    <td scope="row">Nov. 2nd week</td>
    <td>Project Design</td>
    <td colspan="2">
        <ul>
            <li>Design module architecture</li>
            <li>Design Messaging system</li>
        </ul>
    </td>
  </tr>
  <tr>
    <td scope="row">Nov. 3rd week</td>
    <td>Milestone #1</td>
    <td>
        Connect master and workers
        <ul>
            <li>Design message specification</li>
            <li>Make netty server and client works</li>
            <li>Execute master</li>
        </ul>
    </td>
    <td>
        Sort in a worker
        <ul>
            <li>Create input data</li>
            <li>Test sort library</li>
            <li>(if possible) Partition sorted data</li>
        </ul>
    </td>
  </tr>
  <tr>
    <td scope="row">Nov. 4th week</td>
    <td>Progress Report</br>Milestone #2</td>
    <td>Pivoting from sample data</td>
    <td>Sampling and partitioning</td>
  </tr>
  <tr>
    <td scope="row">Dec. 1st week</td>
    <td>Milestone #3</td>
    <td>Connect workers to shuffle</td>
    <td>Merge shuffled data</td>
  </tr>
  <tr>
    <td scope="row">Dec. 2nd week</td>
    <td>Milestone #3</td>
    <td colspan="2">Overall test</td>
  </tr>
  <tr>
    <td scope="row">Dec. 3rd week</td>
    <td>Project Presentation</td>
    <td colspan="2"></td>
  </tr>
</table>


## Document
[Wiki-Page](https://github.com/choisium/433-project/wiki)


## License
[MIT](https://choosealicense.com/licenses/mit/)
