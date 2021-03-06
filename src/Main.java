import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("===== [YSY] Map-matching PilotTest 1-2 ====");
        FileIO fileIO = new FileIO();

        // 도로네트워크
        RoadNetwork roadNetwork = fileIO.generateRoadNetwork();

        // GPS points와 routePoints
        ArrayList<GPSPoint> gpsPointArrayList = new ArrayList<>();
        ArrayList<Point> routePointArrayList = new ArrayList<>(); // 실제 경로의 points!

        GenerateGPSPoint(roadNetwork.linkArrayList, gpsPointArrayList, routePointArrayList);

        // origin route points와 랜덤하게 생성된 GPS points 500ms에 한번씩 출력하기
        for (int i = 0; i < gpsPointArrayList.size(); i++) {
            System.out.println(routePointArrayList.get(i));
            System.out.println(gpsPointArrayList.get(i));
            Thread.sleep(500); // 500ms 마다 출력
        }
        //유림이가 썼던 코드
        Point gpsPoint = new Point(1.0,2.0);
        ArrayList<Link> candidateLink = new ArrayList<>();
        candidateLink = gpsPoint.findRadiusLink(roadNetwork.linkArrayList,roadNetwork.nodeArrayList);
        ArrayList<Point> candidate = new ArrayList<>();
        for(int i=0;i<candidateLink.size();i++)//모든 candidate Link 순회 하며, involving node들만 모아서 'candidate'에 저장
            candidate.addAll(findRadiusPoint(gpsPoint,candidateLink.get(i),2));
    }
    // GPS 포인트 1초에 1개씩 생성하는 함수 : 어차피 여기서만 쓰니까 그냥 Main클래스에 구현했어요~
    private static void GenerateGPSPoint(ArrayList<Link> linkArrayList, ArrayList<GPSPoint> gpsPointArrayList, ArrayList<Point> routePointArrayList) {
        /////////// 과연 1-2에서도 성공할것인가 //////////////////// 이 이하로 아직손도안댐~~~~~~~~~~

        System.out.println("======GPS Data=======");
        boolean isThereEdgeLink;
        Link startLink = new Link(0, 0, 0, 0.0);
        ArrayList<Integer> overlappedNodeIDs = new ArrayList<>();
        ArrayList<Integer> nodeIDs = new ArrayList<>();
        ArrayList<Integer> linkIDs = new ArrayList<>();
        // nodeID 중복허용 모두 저장
        for (Link link : linkArrayList) {
            overlappedNodeIDs.add(link.getStartNodeID());
            overlappedNodeIDs.add(link.getEndNodeID());
        }
        // 중복되지 않는 노드만 저장
        for (int index1 = 0; index1 < overlappedNodeIDs.size(); index1++) {
            for (int index2 = 0; index2 < overlappedNodeIDs.size(); index2++) {
                if (index1 == index2) continue;
                else if (overlappedNodeIDs.get(index1) == overlappedNodeIDs.get(index2)) {
                    break;
                }
                if (index2 == overlappedNodeIDs.size() - 1) {
                    nodeIDs.add(overlappedNodeIDs.get(index1));
                }
            }
        }

        // 만약 노드 중 end이기만 하거나 start이기만 한 node (끝 노드) 가 있다면
        // nodeID가 작은 node를 end/start로 가지는 link가 시작링크
        // end이기만 하거나 start이기만 한 link 가 없다면 0을 startNodeID로 갖는 link가 시작링크
        int minNodeID = 0;
        if (nodeIDs.size() == 2) { // 끝노드 2개인 경우
            minNodeID = (nodeIDs.get(0) < nodeIDs.get(1)) ? nodeIDs.get(0) : nodeIDs.get(1);
        } else if (nodeIDs.size() == 1) { // 끝노드 1개인 경우
            minNodeID = nodeIDs.get(0);
        }
        for (Link link : linkArrayList) {
            if (link.getStartNodeID() == minNodeID || link.getEndNodeID() == minNodeID) {
                startLink = link;
                break;
            }
        }
        // currNodeID를 startNodeID 혹은 endNodeID 로 가지는 Link 중복체크하면서 하나씩 불러오기 (반복)
        int count = 0;
        int currNodeID = startLink.getEndNodeID();
        int timeStamp = 0; // 추후 String으로 변경
        linkIDs.add(startLink.getLinkID());
        while (count < linkArrayList.size()) {
            if (count == linkArrayList.size()) break;

            // startLink의 involvingPoints는 무조건 이용!
            if (count == 0) {
                ArrayList<Point> involvingPoints = startLink.getInvolvingPointList();
                for (Point point : involvingPoints) {
                    routePointArrayList.add(point); // routePointArrayList에 추가
                    GPSPoint gpsPoint = new GPSPoint(timeStamp, point); // GPS 포인트 생성!
                    gpsPointArrayList.add(gpsPoint); // gpsPointArrayList에 추가
                    timeStamp++;
                }
            }
            for (Link link : linkArrayList) {
                // currNodeID를 startNodeID로 가지는 Link 불러오기
                if (link.getStartNodeID() == currNodeID && !linkIDs.contains(link.getLinkID())) {
                    // GPS 생성을 위한 link의 involvingPoints출력
                    linkIDs.add(link.getLinkID()); // 중복검색 방지
                    ArrayList<Point> involvingPoints = link.getInvolvingPointList();
                    for (Point point : involvingPoints) {
                        routePointArrayList.add(point); // routePointArrayList에 추가
                        GPSPoint gpsPoint = new GPSPoint(timeStamp, point); // GPS 포인트 생성!
                        gpsPointArrayList.add(gpsPoint); // gpsPointArrayList에 추가
                        timeStamp++;
                    }
                    currNodeID = link.getEndNodeID();
                }
                // currNodeID를 endNodeID로 가지는 Link 불러오기
                else if (link.getEndNodeID() == currNodeID && !linkIDs.contains(link.getLinkID())) {
                    // GPS 생성을 위한 link의 involvingPoints출력
                    linkIDs.add(link.getLinkID()); // 중복검색 방지
                    ArrayList<Point> involvingPoints = link.getInvolvingPointList();
                    for (int i = involvingPoints.size() - 1; i >= 0; i--) {
                        routePointArrayList.add(involvingPoints.get(i)); // routePointArrayList에 추가
                        GPSPoint gpsPoint = new GPSPoint(timeStamp, involvingPoints.get(i)); // GPS 포인트 생성!
                        gpsPointArrayList.add(gpsPoint); // gpsPointArrayList에 추가
                        timeStamp++;
                    }
                    // 다음 currNode지정: start인 경우에는 그 링크의 endNode를, 반대의 경우에는 startNode를 currNode로 지정
                    currNodeID = link.getStartNodeID();
                }
            }
            count++;
        }
    }public static Double coordDistanceofPoints(Point a, Point b){
        return Math.sqrt(Math.pow(a.getX()-b.getX(),2)+Math.pow(a.getY()-b.getY(),2));
    }//유클리드 거리 구하기

    // 이 아이를 어디 맵매칭클래스 같은거 만들어거 거기에 넣으면 좋지 않을까?하는 생각이어요~
    public static ArrayList<Point> findRadiusPoint(Point center, Link link, Integer Radius){//Link 안, 반경 내 involving node들만 반환
        ArrayList<Point> allInvolvingPoint =link.getInvolvingPointList();
        ArrayList<Point> resultPoint = new ArrayList<>();
        for(int i=0;i<allInvolvingPoint.size();i++){
            if(coordDistanceofPoints(center,allInvolvingPoint.get(i))<=Radius)
                resultPoint.add(allInvolvingPoint.get(i));
        }
        return resultPoint;
    }
}

