package com.example.azgerd.graphics;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Azgerd on 15.02.2018.
 */



class Position{
    int X;
    int Y;

    public Position(){
        this.X = 0;
        this.Y = 0;
    }

    public Position(int x, int y){
        this.X = x;
        this.Y = y;
    }
}

class Button{
    Position start;
    Position end;
    String name;
    int background;
    int activeBackground;
    boolean active;

    public Button(String name){
        this.start = new Position();
        this.end = new Position();
        this.name = name;
        this.background = Color.WHITE;
        this.activeBackground = Color.WHITE;
        this.active = true;
    }

    public Button(String name, int background){
        this.start = new Position();
        this.end = new Position();
        this.name = name;
        this.background = Color.WHITE;
        this.activeBackground = background;
        this.active = true;
    }

    public void Draw(Canvas canvas){
        Paint paint = new Paint();
        Path path = new Path();

        if(this.active)
            paint.setColor(this.activeBackground);
        else
            paint.setColor(this.background);

        path.moveTo(this.start.X, this.start.Y);
        path.lineTo(this.end.X, this.start.Y);
        path.lineTo(this.end.X, this.end.Y);
        path.lineTo(this.start.X, this.end.Y);
        canvas.drawPath(path, paint);

        Rect mTextBoundRect = new Rect();
        paint.setColor(Color.BLACK);
        paint.setTextSize((int)((this.end.Y-this.start.Y)*0.5));
        paint.getTextBounds(this.name, 0, this.name.length(), mTextBoundRect);
        float mTextWidth = paint.measureText(this.name);
        float mTextHeight = mTextBoundRect.height();
        canvas.drawText(this.name, this.start.X+(this.end.X-this.start.X)/2 - (mTextWidth / 2f), this.start.Y + (this.end.Y-this.start.Y)/2 + (mTextHeight /2f), paint);

    }
}

class AreaChart{
    ArrayList array;
    int colorLine;
    int colorBackground;
    boolean viewLine;
    boolean viewArea;

    public AreaChart(int colorL, int colorB){
        this.colorBackground = colorB;
        this.colorLine = colorL;
        this.array = new ArrayList();
        this.viewArea = true;
        this.viewLine = true;
    }
}

public class Chart extends View {

    ArrayList<AreaChart> charts = new ArrayList<>();
    ArrayList<Button> buttons = new ArrayList<>();

    final Random random = new Random();

    Position[] chartPlatform = {new Position(50, 50), new Position()};
    int chartPlatformBackground = Color.WHITE;

    int movePoint=0;//Точка касания определяющая действия для движения

    int x, y;//Координаты нажатия
    int countIterationCharts;//Временной интервал графика


    int canvasWidth;
    int canvasHeight;
    int minWidthChart;
    int minHeightChart;
    boolean horisont = false;
    int buttonClick = -1;//Какая кнопка нажата

    int buttonDeltaY = 0;
    int buttonDeltaX = 0;

    Position borderStart = new Position(50, 50);//ограничения на передвижения графика
    Position borderEnd = new Position();

    //начальные отступы внутри графика
    int paddingLeft = 100;
    int paddingRight = 100;
    int paddingTop = 100;
    int paddingBottom = 100;

    int deltaMoveChart = 0;//сдвиг графика
    float stepX = 0;//Длина временного шага
    float stepY = 0;

    float FunctionPM(float num){
        return num<0?-num:num;
    }
    public Chart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        charts.add(new AreaChart(Color.RED, Color.RED));
        charts.add(new AreaChart(Color.GREEN, Color.GREEN));
        charts.add(new AreaChart(Color.BLUE, Color.BLUE));
        charts.add(new AreaChart(Color.YELLOW, Color.YELLOW));

        countIterationCharts = random.nextInt(40)+10;

        for (int i = 0; i < charts.size(); i++) {
            for (int j = 0; j < countIterationCharts; j++) {
                charts.get(i).array.add(random.nextInt(20));
            }
        }

        for (int i = 0; i < charts.size(); i++) {
            buttons.add(new Button("Chart "+ (i+1) +" line", Color.GREEN));
            buttons.add(new Button("Chart "+ (i+1) +" area", Color.GREEN));
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        Path path = new Path();
        int lineStrokeWidth = 10;

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        //Определяем ориентацию телефона
        if(canvasWidth>canvasHeight) {
            horisont = true;
            minWidthChart = 750;
            minHeightChart = 450;
            borderEnd = new Position(canvasWidth/4*3-50, canvasHeight-50);
        }
        else{
            minWidthChart = 500;
            minHeightChart = 300;
            borderEnd = new Position(canvasWidth-50, canvasHeight/4*3-50);
        }



        if(chartPlatform[1].X == 0){//Ставлю изначальные размеры для прорисовки платформы графика
            chartPlatform[1].X = chartPlatform[0].X+minWidthChart;
            chartPlatform[1].Y = chartPlatform[0].Y+minHeightChart;
        }

        paddingRight = (int)((chartPlatform[1].X - chartPlatform[0].X)*0.0);//отступы в графике
        paddingTop = (int)((chartPlatform[1].Y - chartPlatform[0].Y)*0.05);
        paddingLeft = (int)((chartPlatform[1].X - chartPlatform[0].X)*0.1);
        paddingBottom = (int)((chartPlatform[1].Y - chartPlatform[0].Y)*0.1);

        paint.setStrokeWidth(lineStrokeWidth);//Рисую заливку платформы под график
        paint.setColor(chartPlatformBackground);
        path.moveTo(chartPlatform[0].X, chartPlatform[0].Y);
        path.lineTo(chartPlatform[1].X, chartPlatform[0].Y);
        path.lineTo(chartPlatform[1].X, chartPlatform[1].Y);
        path.lineTo(chartPlatform[0].X, chartPlatform[1].Y);
        canvas.drawPath(path, paint);
        path.reset();


        //Draw Chart
        paint.setStrokeWidth(5);

        //Нижняя толстая линия графика
        paint.setColor(Color.rgb(100, 100, 100));
        canvas.drawLine(chartPlatform[0].X+paddingLeft, chartPlatform[1].Y-paddingBottom, chartPlatform[1].X-paddingRight, chartPlatform[1].Y-paddingBottom, paint);//x

        paint.setColor(Color.rgb(200, 200, 200));
        stepX = ((float)(chartPlatform[1].X-chartPlatform[0].X-paddingLeft-paddingRight))/10;
        float summStepX = chartPlatform[0].X+50 + stepX;

        int minData;
        int maxData;
        int heightData;

        maxData = (int)(charts.get(0).array.get(0));
        minData = (int)(charts.get(0).array.get(0));

        for (int i = 0; i < charts.size(); i++) {
            for (int j = 0; j < charts.get(i).array.size(); j++) {
                if((int)(charts.get(i).array.get(j))>maxData){
                    maxData = (int)(charts.get(i).array.get(j));
                }
                if((int)(charts.get(i).array.get(j))<minData){
                    minData = (int)(charts.get(i).array.get(j));
                }
            }
        }
        heightData = maxData-minData;//Количество делений по Y


        stepY = ((float)(chartPlatform[1].Y-chartPlatform[0].Y-paddingTop-paddingBottom))/heightData;//Длина шага по Y
        float summStepY =chartPlatform[0].Y+paddingTop;

        //Рисвание горизонтальных линий
        for (int i = 0; i < 9; i++) {
            canvas.drawLine(chartPlatform[0].X+paddingLeft, summStepY+(int)(((float)(chartPlatform[1].Y-chartPlatform[0].Y-paddingTop-paddingBottom))/9*i), chartPlatform[1].X-paddingRight, summStepY+(int)(((float)(chartPlatform[1].Y-chartPlatform[0].Y-paddingBottom-paddingTop))/9*i), paint);
        }

        summStepX = chartPlatform[0].X+paddingLeft;
        summStepY =chartPlatform[1].Y-paddingBottom;

        //area - Рисование заливок графиков
        int deltaItarationMove = -(int)(deltaMoveChart/stepX);
        //int deltaItarationMoveMax = deltaItarationMove+11+((int)deltaMoveChart%(int)stepX!=0?1:0);
        int deltaItarationMoveMax = deltaItarationMove+12-((int)deltaMoveChart%(int)stepX==0?1:0);
        /*
        if((int)deltaMoveChart%(int)stepX<2 || (int)deltaMoveChart%(int)stepX>stepX-2)
            deltaMoveChart++;
            */
        if(deltaItarationMoveMax>charts.get(0).array.size()){
            deltaItarationMoveMax = charts.get(0).array.size();
        }

        int newYStartPosition = 0;
        float x1 = 0;
        float y1 = 0;
        float x2 = 0;
        float y2 = 0;
        float x3 = 0;

        for (int i = 0; i < charts.size(); i++) {
            if(charts.get(i).viewArea) {
                paint.setColor(charts.get(i).colorBackground);
                paint.setAlpha(50);
                x1 = ((summStepX + stepX * deltaItarationMove)+deltaMoveChart);
                y1 = (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMove) - minData) * stepY);
                x2 = ((summStepX + stepX * (deltaItarationMove+1))+deltaMoveChart);
                y2 = (summStepY - ((float) (int) charts.get(i).array.get(1+deltaItarationMove) - minData) * stepY);
                x3 = summStepX;

                //int newYStartPosition = (int)(/**/(summStepX - ((summStepX + stepX * deltaItarationMove)+deltaMoveChart))*((summStepY - ((float) (int) charts.get(i).array.get(1+deltaItarationMove) - minData) * stepY) - (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMove) - minData) * stepY))/(((summStepX + stepX * deltaItarationMove)+deltaMoveChart)-((summStepX + stepX * deltaItarationMove)+deltaMoveChart)) /**/);
                //int newYStartPosition = (int)(FunctionPM(x3-x1)*FunctionPM(y2-y1)/FunctionPM(x2-x1));
                newYStartPosition = (int)((x3-x1)*(y2-y1)/(x2-x1) + y1);

                //path.moveTo((int) (summStepX + stepX * deltaItarationMove)+deltaMoveChart, (int) (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMove) - minData) * stepY));
                path.moveTo(summStepX, newYStartPosition);

                for (int j = 1+deltaItarationMove; j < deltaItarationMoveMax-1; j++) {
                    path.lineTo((int) (summStepX + stepX * j)+deltaMoveChart, (int) (summStepY - ((float) (int) charts.get(i).array.get(j) - minData) * stepY));
                }

                x1 = (summStepX + stepX * (deltaItarationMoveMax-2))+deltaMoveChart;
                y1 = (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMoveMax-2) - minData) * stepY);
                x2 = (summStepX + stepX * (deltaItarationMoveMax-1))+deltaMoveChart;//yes
                y2 = (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMoveMax-1) - minData) * stepY);
                x3 = chartPlatform[1].X - paddingRight;//yes
                newYStartPosition = (int)((x3-x1)*(y2-y1)/(x2-x1) + y1);

                path.lineTo(x3,newYStartPosition);

                path.lineTo(x3, chartPlatform[1].Y - paddingBottom);
                path.lineTo(summStepX, chartPlatform[1].Y - paddingBottom);
                path.lineTo(summStepX, newYStartPosition);
                canvas.drawPath(path, paint);
                path.reset();
                paint.setAlpha(255);
            }
        }

        //lines - рисование линий графиков
        for (int i = 0; i < charts.size(); i++) {
            if(charts.get(i).viewLine) {

                x1 = ((summStepX + stepX * deltaItarationMove)+deltaMoveChart);
                y1 = (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMove) - minData) * stepY);
                x2 = ((summStepX + stepX * (deltaItarationMove+1))+deltaMoveChart);
                y2 = (summStepY - ((float) (int) charts.get(i).array.get(1+deltaItarationMove) - minData) * stepY);
                x3 = summStepX;
                newYStartPosition = (int)((x3-x1)*(y2-y1)/(x2-x1) + y1);
                paint.setColor(charts.get(i).colorLine);
                canvas.drawLine(summStepX, newYStartPosition, summStepX + stepX * (deltaItarationMove + 1) + deltaMoveChart, (int) (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMove + 1) - minData) * stepY), paint);
                for (int j = deltaItarationMove+1; j < deltaItarationMoveMax-2; j++) {
                    canvas.drawLine(summStepX + stepX * j + deltaMoveChart, (int) (summStepY - ((float) (int) charts.get(i).array.get(j) - minData) * stepY), summStepX + stepX * (j + 1) + deltaMoveChart, (int) (summStepY - ((float) (int) charts.get(i).array.get(j + 1) - minData) * stepY), paint);
                }

                x1 = (summStepX + stepX * (deltaItarationMoveMax-2))+deltaMoveChart;
                y1 = (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMoveMax-2) - minData) * stepY);
                x2 = (summStepX + stepX * (deltaItarationMoveMax-1))+deltaMoveChart;//yes
                y2 = (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMoveMax-1) - minData) * stepY);
                x3 = chartPlatform[1].X - paddingRight;//yes
                newYStartPosition = (int)((x3-x1)*(y2-y1)/(x2-x1) + y1);
                if(deltaMoveChart%(int)stepX == 0){
                    canvas.drawLine(summStepX + stepX * (deltaItarationMoveMax-2) + deltaMoveChart, (int) (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMoveMax-2) - minData) * stepY), summStepX + stepX * (deltaItarationMoveMax-1) + deltaMoveChart, (int) (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMoveMax-1) - minData) * stepY), paint);
                }
                else {
                    canvas.drawLine(summStepX + stepX * (deltaItarationMoveMax - 2) + deltaMoveChart, (int) (summStepY - ((float) (int) charts.get(i).array.get(deltaItarationMoveMax - 2) - minData) * stepY), x3, newYStartPosition, paint);
                }
            }
        }


        //задача позици для кнопок
        if(horisont){
            int buttonStepY = 50;
            for(Button button:buttons){
                button.start.X = canvasWidth/4*3;
                button.end.X = canvasWidth-50;
                button.start.Y = buttonStepY+buttonDeltaY;
                button.end.Y = buttonStepY+100+buttonDeltaY;
                buttonStepY += 150;
            }
        }
        else{
            int buttonStepY = canvasHeight/4*3;
            int buttonStepX = 50;
            for (int i = 0; i < buttons.size(); i++) {
                if(i>0 && i%3 == 0){
                    buttonStepY = canvasHeight/4*3;
                    buttonStepX += (canvasWidth-150)/2 +50;
                }
                buttons.get(i).start.X = buttonStepX + buttonDeltaX;
                buttons.get(i).end.X = (canvasWidth-150)/2 + buttonStepX + buttonDeltaX;
                buttons.get(i).start.Y = buttonStepY;
                buttons.get(i).end.Y = buttons.get(i).start.Y+(canvasHeight/4-70)/3;
                buttonStepY += (canvasHeight/4-70)/3+10;
            }
        }

        //Рисование кнопок
        for(Button button:buttons){
            button.Draw(canvas);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            x = (int) event.getX();
            y = (int) event.getY();

//////////////Нажали на график
            if(x>chartPlatform[0].X && x<chartPlatform[1].X && y>chartPlatform[0].Y && y<chartPlatform[1].Y) {
                if (x > chartPlatform[0].X && x < chartPlatform[0].X + 100 && y > chartPlatform[0].Y && y < chartPlatform[0].Y + 100) {
                    movePoint = 1;//Left-UP
                } else if (x < chartPlatform[1].X && x > chartPlatform[1].X - 100 && y > chartPlatform[0].Y && y < chartPlatform[0].Y + 100) {
                    movePoint = 2;//Right-UP
                } else if (x > chartPlatform[0].X && x < chartPlatform[0].X + 100 && y < chartPlatform[1].Y && y > chartPlatform[1].Y - 100) {
                    movePoint = 3;//Left-Down
                } else if (x < chartPlatform[1].X && x > chartPlatform[1].X - 100 && y < chartPlatform[1].Y && y > chartPlatform[1].Y - 100) {
                    movePoint = 4;//Right-Down
                } else if (x > chartPlatform[0].X && x < chartPlatform[1].X && y > chartPlatform[0].Y && y < chartPlatform[0].Y + 100) {
                    movePoint = 5;//UP
                } else if (x > chartPlatform[0].X && x < chartPlatform[1].X && y > chartPlatform[1].Y - 100 && y < chartPlatform[1].Y) {
                    movePoint = 6;//Down
                } else if (x > chartPlatform[0].X && x < chartPlatform[0].X + 100 && y > chartPlatform[0].Y && y < chartPlatform[1].Y) {
                    movePoint = 7;//Left
                } else if (x > chartPlatform[1].X - 100 && x < chartPlatform[1].X && y > chartPlatform[0].Y && y < chartPlatform[1].Y) {
                    movePoint = 8;//Right
                } else {//если нажали в центр
                    if(chartPlatform[1].X - chartPlatform[0].X>borderEnd.X-100){//если график растянут по всей ширине
                        movePoint = 11;//Изменение данных графика по горизонтали
                    }
                    else {//иначе просто перемещаем его в пределах разрешенной области (BorderStart, BorderEnd)
                        movePoint = 9;
                    }
                }
            }//Нажали вне графика
            else{
                movePoint = 10;
                for (int i = 0; i < buttons.size(); i++) {//Если нажали на кнопку, то фиксируем ее индекс
                    if (x > buttons.get(i).start.X && x < buttons.get(i).end.X && y > buttons.get(i).start.Y && y < buttons.get(i).end.Y) {
                        buttonClick = i;
                    }
                }
            }
        }
        else if(event.getAction() == MotionEvent.ACTION_UP) {
            x = (int) event.getX();
            y = (int) event.getY();
            for (int i = 0; i < buttons.size(); i++) {//Проверяем отпустили на той же кнопке или нет
                if (x > buttons.get(i).start.X && x < buttons.get(i).end.X && y > buttons.get(i).start.Y && y < buttons.get(i).end.Y && i == buttonClick) {
                    buttons.get(i).active=!buttons.get(i).active;
                    if(i%2==0)
                        charts.get(i/2).viewLine = !charts.get(i/2).viewLine;
                    else
                        charts.get(i/2).viewArea = !charts.get(i/2).viewArea;
                }
            }
            buttonClick = -1;//Очищаем индекс нажатой кнопки
            movePoint = 0;//и так же нажатой позиции управления
        }//Если движение
        else{
            int newX = (int) event.getX();
            int newY = (int) event.getY();
            int deltaX = newX - x;//Разница между прошлым положением касания и нынешним
            int deltaY = newY - y;

            switch(movePoint){//до 9 позиции изменяем размеры графика
                case 1:
                    chartPlatform[0].Y += deltaY;
                    y = newY;
                    chartPlatform[0].X += deltaX;
                    x = newX;
                    break;
                case 2:
                    chartPlatform[0].Y += deltaY;
                    y = newY;
                    chartPlatform[1].X += deltaX;
                    x = newX;
                    break;
                case 3:
                    chartPlatform[1].Y += deltaY;
                    y = newY;
                    chartPlatform[0].X += deltaX;
                    x = newX;
                    break;
                case 4:
                    chartPlatform[1].Y += deltaY;
                    y = newY;
                    chartPlatform[1].X += deltaX;
                    x = newX;
                    break;
                case 5:
                    chartPlatform[0].Y += deltaY;
                    y = newY;
                    break;
                case 6:
                    chartPlatform[1].Y += deltaY;
                    y = newY;
                    break;
                case 7:
                    chartPlatform[0].X += deltaX;
                    x = newX;
                    break;
                case 8:
                    chartPlatform[1].X += deltaX;
                    x = newX;
                    break;
                case 9://Изменяем положение графика
                    chartPlatform[0].Y += deltaY;
                    chartPlatform[0].X += deltaX;
                    chartPlatform[1].Y += deltaY;
                    chartPlatform[1].X += deltaX;
                    y = newY;
                    x = newX;
                    break;
                case 10://Прокрутка кнопок
                    if(buttonClick==-1) {//Если не нажата кнопка то вращаем
                        if (buttons.get(buttons.size() - 1).end.Y - buttons.get(0).start.Y > canvasHeight - 100) {
                            buttonDeltaY += deltaY;
                            y = newY;
                            if (buttons.get(0).start.Y + deltaY > 50) {
                                buttonDeltaY -= buttons.get(0).start.Y + deltaY - 50;
                                y -= buttons.get(0).start.Y + deltaY - 50;
                            }

                            if (buttons.get(buttons.size() - 1).end.Y + deltaY < canvasHeight - 50) {
                                buttonDeltaY -= buttons.get(buttons.size() - 1).end.Y + deltaY - canvasHeight + 50;
                                y -= buttons.get(buttons.size() - 1).end.Y + deltaY - canvasHeight + 50;
                            }
                        }
                        if (buttons.get(buttons.size() - 1).end.X - buttons.get(0).start.X > canvasWidth - 100) {
                            buttonDeltaX += deltaX;
                            x = newX;
                            if (buttons.get(0).start.X + deltaX > 50) {
                                buttonDeltaX -= buttons.get(0).start.X + deltaX - 50;
                                x -= buttons.get(0).start.X + deltaX - 50;
                            }

                            if (buttons.get(buttons.size() - 1).end.X + deltaX < canvasWidth - 50) {
                                buttonDeltaX -= buttons.get(buttons.size() - 1).end.X + deltaX - canvasWidth + 50;
                                x -= buttons.get(buttons.size() - 1).end.X + deltaX - canvasWidth + 50;
                            }
                        }
                    }//Иначе если нажата какая-либо кнопка
                    else{
                        if(deltaX>50 || deltaX<-50 || deltaY>50 || deltaY<-50) {//Если начали движение от кнопок, то ставим положения ненажатой кнопки
                            buttonClick = -1;
                            x = newX;
                            y = newY;
                        }
                    }
                    break;
                case 11://Смещение шкалы времени
                    deltaMoveChart += deltaX;
                    x = newX;
                    y = newY;
                    if(deltaMoveChart>0){//Ограничиваем движение гравика его шкалой времени
                        x -= deltaMoveChart;
                        deltaMoveChart = 0;
                    }
                    if(deltaMoveChart<-1*((countIterationCharts-1)*stepX-(chartPlatform[1].X-chartPlatform[0].X-paddingRight-paddingLeft))){
                        x -= deltaMoveChart;
                        deltaMoveChart = (int)(-1*((countIterationCharts-1)*stepX-(chartPlatform[1].X-chartPlatform[0].X-paddingRight-paddingLeft)));
                    }

                    break;
            }

            //Проверки на ограничения по изменению размера и позиции графика
            if(chartPlatform[0].Y<borderStart.Y){
                y += borderStart.Y-chartPlatform[0].Y;
                if(movePoint == 9)
                    chartPlatform[1].Y += borderStart.Y-chartPlatform[0].Y;
                chartPlatform[0].Y += borderStart.Y-chartPlatform[0].Y;
            }
            if(chartPlatform[1].Y > borderEnd.Y){
                y += borderEnd.Y-chartPlatform[1].Y;
                if(movePoint == 9)
                    chartPlatform[0].Y += borderEnd.Y-chartPlatform[1].Y;
                chartPlatform[1].Y +=borderEnd.Y-chartPlatform[1].Y;
            }
            if(chartPlatform[1].X > borderEnd.X){
                x += borderEnd.X-chartPlatform[1].X;
                if(movePoint == 9)
                    chartPlatform[0].X += borderEnd.X-chartPlatform[1].X;
                chartPlatform[1].X +=borderEnd.X-chartPlatform[1].X;
            }
            if(chartPlatform[0].X<borderStart.X){
                x += borderStart.X-chartPlatform[0].X;
                if(movePoint == 9)
                    chartPlatform[1].X += borderStart.X-chartPlatform[0].X;
                chartPlatform[0].X += borderStart.X-chartPlatform[0].X;
            }
            if(chartPlatform[1].X-chartPlatform[0].X < minWidthChart){
                if(movePoint == 2 || movePoint == 4 || movePoint == 8){
                    x += minWidthChart-(chartPlatform[1].X-chartPlatform[0].X);
                    chartPlatform[1].X += minWidthChart-(chartPlatform[1].X-chartPlatform[0].X);
                }
                else if(movePoint == 1 || movePoint == 3 || movePoint == 7){
                    x -= minWidthChart-(chartPlatform[1].X-chartPlatform[0].X);
                    chartPlatform[0].X -= minWidthChart-(chartPlatform[1].X-chartPlatform[0].X);
                }
            }
            if(chartPlatform[1].Y-chartPlatform[0].Y < minHeightChart){
                if(movePoint == 1 || movePoint == 2 || movePoint == 5){
                    y -= minHeightChart-(chartPlatform[1].Y-chartPlatform[0].Y);
                    chartPlatform[0].Y -= minHeightChart-(chartPlatform[1].Y-chartPlatform[0].Y);
                }
                else if(movePoint == 3 || movePoint == 4 || movePoint == 6){
                    y += minHeightChart-(chartPlatform[1].Y-chartPlatform[0].Y);
                    chartPlatform[1].Y += minHeightChart-(chartPlatform[1].Y-chartPlatform[0].Y);
                }
            }
        }

        invalidate();
        return true;
    }
}
