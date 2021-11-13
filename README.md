# 433-project

This is a repository for CSED434 project.  
Goal: Implement distributed sorting key/value records stored on multiple machines.  

## Team members

Team color: Blue  
1. Soomin Choi (20160169)
2. Subin Park (20180953)


## Plan

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