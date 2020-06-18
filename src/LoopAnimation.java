public class LoopAnimation {
    public static void main(String[] args) {
        //Specify canvas particular size
        //PennDraw defines space from 0 to 1 by default
        PennDraw.setCanvasSize(500, 500);
        PennDraw.setScale(0, 500);
        PennDraw.enableAnimation(20);

        /*
        for(int x = 0; x < 500; x = x + 1) {
            PennDraw.clear(0, 10, 30);
            PennDraw.setPenColor(PennDraw.WHITE);
            PennDraw.circle(x, 250, 50);
            PennDraw.advance();
        }

         */
//        int x = 0;
//        int deltaX = 1;
//        while(true) {
//            PennDraw.clear(0, 10, 30);
//            PennDraw.setPenColor(PennDraw.WHITE);
//            PennDraw.circle(x, 250 * (1 + (Math.sin(x / 250.0 * 3.14159))), 50);
//            x = x + deltaX;
//            if (x <= 0) {
//                deltaX = 1;
//            }
//            else if (x >= 500) {
//                deltaX = -1;
//            }
//            PennDraw.advance();
//        }
        String[] imageNames = new String[8];
        for (int i = 0; i < 8; i++) {
            imageNames[i] = "run/run" + (i + 1) + ".png"; //"run/run1.png"
        }
        int index = 0;
        int x = 0;
        while(true) {
            PennDraw.clear(PennDraw.WHITE);
            PennDraw.picture(x, 250, imageNames[index]);
            PennDraw.advance();
            index = (index + 1) % 8;
            x = (x + 10) % 550;
        }
    }
}
