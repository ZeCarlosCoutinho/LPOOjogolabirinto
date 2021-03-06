package maze.logic;
import java.util.*;

import exceptions.TooManyDragonsException;

//Algoritmo da autoria de Henrique Ferrolho
public class MazeGenerator {
	private Tabuleiro maze;
	private Saida exit;
	private Heroi hero;
	private Espada sword;
	private Dragao[] dragons;
	private VisitedCells visitedCells;
	private Celula guideCell;
	private Stack<Celula> lastCells;
	private Random generator;
	
	/**
	 * @brief Retorna saída do tabuleiro criado aleatoriamente
	 * @return Saída
	 */
	public Saida getExit() {
		return exit;
	}
	
	/**
	 * @brief Retorna heroi do tabuleiro criado aleatoriamente 
	 * @return Herói
	 */
	public Heroi getHero() {
		return hero;
	}

	/**
	 * @brief Retorna espada do tabuleiro criado aleatoriamente 
	 * @return Espada
	 */
	public Espada getSword() {
		return sword;
	}
	

	/**
	 * @brief Retorna array de dragoes com os dragoes do tabuleiro criado aleatoriamente 
	 * @return Dragao[]
	 */
	public Dragao[] getDragons() {
		return dragons;
	}
	
	/**
	 * @return array2D das visitedCells
	 */
	public VisitedCells getVisitedCells() {
		return visitedCells;
	}
	
	/**
	 * @return GuideCell (presente na visitedCells)
	 */
	public Celula getGuideCell() {
		return guideCell;
	}
	
	/** 
	 * @return stack das celulas visitadas
	 */
	public Stack<Celula> getLastCells() {
		return lastCells;
	}
	
	/**
	 * @return gerador random
	 */
	public Random getGenerator() {
		return generator;
	}
	
	/**
	 * @return Tabuleiro que foi/ vai ser gerado aleatoriamentente
	 */
	public Tabuleiro getMaze() {
		return maze;
	}
	
	/**
	 * @brief Construtor do MazeGenerator com o size definido por quem chama
	 * @param size
	 */
	public MazeGenerator(int size)
	{
		this.generator = new Random();
		
		this.maze = new Tabuleiro(size);
		this.maze.make_quadriculado();
		
		this.visitedCells = new VisitedCells((size-1)/2);
		
		//Coloca a celula guide numa casa aleatoria
		this.guideCell = iniciar_guideCell();
		escreveGuideVisitedCells();

		this.exit = iniciar_saida(guideCell);
		
		this.lastCells = new Stack<Celula>();
		this.lastCells.push(guideCell);
		
	}
	
	/**
	 * @brief construtor default do mazeGenerator
	 */
	public MazeGenerator() {
		
	}

	/**
	 * @brief Constroi o novo Maze. ORDEM DE COLOCACAO: 1 - SAIDA, 2 - HEROI, 3 - ESPADA, 4 - DRAGOES
	 * @param size
	 * @return grid do Maze
	 * @throws Exception 
	 */
	public char[][] buildMaze(int size) throws TooManyDragonsException
	{
		this.generator = new Random();

		this.maze = new Tabuleiro(size);
		this.maze.make_quadriculado();

		this.visitedCells = new VisitedCells((size-1)/2);
	

		//Coloca a celula guide numa casa aleatoria
		this.guideCell = iniciar_guideCell();
		escreveGuideVisitedCells();

		//Inicia os elementos que vao aparecer no tabuleiro
		this.hero = new Heroi();
		this.sword = new Espada();
		this.dragons = new Dragao[4];
		this.exit = new Saida();
		this.exit = iniciar_saida(guideCell);

		//Inicia a stack de celulas, de modo a o gerador saber a ordem das casas em que passou
		this.lastCells = new Stack<Celula>();
		this.lastCells.push(new Celula(guideCell));
		
		//Cria os caminhos do labirinto
		this.abreCaminho();
		
		this.hero = iniciar_heroi();
		this.sword = iniciar_espada();
		iniciar_dragoes();
		
		//Imprime os elementos no tabuleiro
		this.maze.setChar('S', exit.posX, exit.posY);
		this.maze.setChar('H', hero.posX, hero.posY);
		this.maze.setChar('E', sword.posX, sword.posY);
		for(int i = 0; i < dragons.length; i++)
		{
			if(dragons[i] != null)
				this.maze.setChar('D', dragons[i].posX, dragons[i].posY);
		}
		
		return this.maze.getBoard();
	}
	
	/**
	 * @brief Constroi um novo maze de tamanho size, e com numDragoes indicado
	 * @param size
	 * @param numDragoes
	 * @return grid do Maze
	 * @throws TooManyDragonsException
	 */
	public char[][] buildMaze(int size, int numDragoes) throws TooManyDragonsException
	{
		this.generator = new Random();

		this.maze = new Tabuleiro(size);
		this.maze.make_quadriculado();

		this.visitedCells = new VisitedCells((size-1)/2);
	

		//Coloca a celula guide numa casa aleatoria
		this.guideCell = iniciar_guideCell();
		escreveGuideVisitedCells();

		//Inicia os elementos que vao aparecer no tabuleiro
		this.hero = new Heroi();
		this.sword = new Espada();
		this.dragons = new Dragao[numDragoes];
		this.exit = new Saida();
		this.exit = iniciar_saida(guideCell);
		this.hero = iniciar_heroi();
		this.sword = iniciar_espada();
		iniciar_dragoes();

		//Inicia a stack de celulas, de modo a o gerador saber a ordem das casas em que passou
		this.lastCells = new Stack<Celula>();
		this.lastCells.push(new Celula(guideCell));
		
		//Cria os caminhos do labirinto
		this.abreCaminho();
		
		//Imprime os elementos no tabuleiro
		this.maze.setChar('S', exit.posX, exit.posY);
		this.maze.setChar('H', hero.posX, hero.posY);
		this.maze.setChar('E', sword.posX, sword.posY);
		for(int i = 0; i < dragons.length; i++)
		{
			this.maze.setChar('D', dragons[i].posX, dragons[i].posY);
		}
		
		return this.maze.getBoard();
	}
	
	/**
	 * @brief Decide aonde vai comecar a guide cell (junto a uma das paredes)
	 * @return Celula com as coordenadas da guideCell
	 */
	public Celula iniciar_guideCell() //Cria uma guideCell para colocar em visitedCells
	{
		Celula cell = new Celula(0,0);
		int tipo_iniciar = generator.nextInt(4);
		switch(tipo_iniciar)
		{
		case 0: //Junto a Coluna da esquerda
			cell.y = generator.nextInt(visitedCells.getTam());
			break;
		case 1: //Junto a Linha de cima
			cell.x = generator.nextInt(visitedCells.getTam());
			break;
		case 2: //Junto a Linha de baixo
			cell.x = generator.nextInt(visitedCells.getTam());
			cell.y = visitedCells.getTam() -1;
			break;
		case 3: //Junto a Coluna da direita
			cell.x = visitedCells.getTam() -1;
			cell.y = generator.nextInt(visitedCells.getTam());
			break;
		default:
			break;
		}
		
		return cell;
	}
	
	/**
	 * @brief Inicia a saida numa das paredes adjacentes a guideCell
	 * @param cell
	 * @return Saida
	 */
	public Saida iniciar_saida(Celula cell)
	{
		Saida s;
		//ENCOSTADO COLUNA ESQUERDA
		if(cell.x == 0)
		{
			if(cell.y == 0) //Tambem encostado a Linha Cima
			{
				if(generator.nextBoolean())
				{
					s = new Saida(0, 1);
				}
				else
				{
					s = new Saida(1, 0);
				}
				return s;
			}
			else if(cell.y == visitedCells.getTam()-1) //Tambem encostado a Linha Baixo
			{
				if(generator.nextBoolean())
				{
					s = new Saida(0, maze.getTamy()-2);
				}
				else
				{
					s = new Saida(1, maze.getTamy()-1);
				}
				return s;
			}
			else
			{
				s = new Saida(0, converter_VisToMaze(cell.y));
				return s;
			}
		}
		//ENCOSTADO COLUNA DIREITA
		else if(cell.x == visitedCells.getTam()-1)
		{
			if(cell.y == 0) //Tambem encostado a Linha Cima
			{
				if(generator.nextBoolean())
				{
					s = new Saida(maze.getTamx()-1, 1);
				}
				else
				{
					s = new Saida(maze.getTamx()-2, 0);
				}
				return s;
			}
			else if(cell.y == visitedCells.getTam()-1) //Tambem encostado a Linha Baixo
			{
				if(generator.nextBoolean())
				{
					s = new Saida(maze.getTamx()-1, maze.getTamy()-2);
				}
				else
				{
					s = new Saida(maze.getTamx()-2, maze.getTamy()-1);
				}
				return s;
			}
			else
			{
				s = new Saida(maze.getTamx()-1, converter_VisToMaze(cell.y));
				return s;
			}
		}
		//ENCOSTADO LINHA CIMA
		else if(cell.y == 0)
		{
			s = new Saida(converter_VisToMaze(cell.x), 0);
			return s;
		}
		//ENCOSTADO LINHA BAIXO
		else if(cell.y == visitedCells.getTam()-1)
		{
			s = new Saida(converter_VisToMaze(cell.x), maze.getTamy()-1);
			return s;
		}
		else
		{
			//excepTion //VAMOS TER DE CRIAR EXCEPCAO, MAS NAO POR ENQUANTO
			s = new Saida(1, 0); //CODIGO PROVISORIO
			return s;
		}
	}
	
	/**
	 * @brief Coloca o heroi num espa�o random
	 * @return Heroi
	 */
	public Heroi iniciar_heroi()
	{
		//Cria uma celula
		Celula posicao = new Celula();

		//Coloca o heroi numa casa aleatoria
		do
		{
			posicao.x = generator.nextInt(maze.getTamx());
			posicao.y = generator.nextInt(maze.getTamy());
		}
		while(maze.getChar(posicao) != ' '); //Enquanto o heroi nao tiver num espaco

		Heroi hero = new Heroi(posicao);
		return hero;
	}
	
	/**
	 * @brief Coloca a espada num espaco em que ainda nao esteja o heroi
	 * @return Espada
	 */
	public Espada iniciar_espada()
	{
		Celula posicao = new Celula();
		
		do
		{
			posicao.x = generator.nextInt(maze.getTamx());
			posicao.y = generator.nextInt(maze.getTamy());
		}
		while(!coloca_Espada(posicao));
		
		Espada sword = new Espada(posicao);
		return sword;
	}
	
	/**
	 * @brief Coloca um dragao numa posicao que nao mate o heroi nem que tenha a espada
	 * @return Dragao colocado
	 * @throws TooManyDragonsException
	 */
	public Dragao iniciar_dragao() throws TooManyDragonsException
	{
		Celula posicao = new Celula();
		int tentativas = 0;
		int tentativasMaximas = 300;
		do
		{
			posicao.x = generator.nextInt(maze.getTamx());
			posicao.y = generator.nextInt(maze.getTamy());
			
			tentativas++;
		}
		while(!coloca_Dragao(posicao) && tentativas < tentativasMaximas);
		
		if(tentativas == tentativasMaximas)
			//throw new TooManyDragonsException();
			return null;
		//So cria o dragao quando tem a certeza que o pode colocar na posicao da Celula
		Dragao dragon = new Dragao(posicao);
		return dragon;
	}
	
	/**
	 * @brief Coloca todos os dragoes em espacos livres
	 * @throws TooManyDragonsException
	 */
	public void iniciar_dragoes() throws TooManyDragonsException
	{
		//Criar um dragao por posicao do array
		for(int i = 0; i < dragons.length ; i++)
		{
				dragons[i] = iniciar_dragao();
		}
		
		return;
	}

	/**
	 * @brief Verifica se a espada pode ser colocada na cell indicada
	 * @param cell
	 * @return true se conseguiu colocar, false caso contrario
	 */
	public boolean coloca_Espada(Celula cell)
	{
		//Se a espada nao estiver em cima de um espaco
		if(maze.getChar(cell) != ' ')
			return false;
		
		//Se a espada estiver em cima do heroi
		if(cell.equals(hero))
			return false;
		
		//Se o dragao estiver em cima da espada
		if(cell.equals(sword))
			return false;
		
		
		return true;
	}
	
	/**
	 * @brief Verifica se consegue colocar um dragao na cell indicada
	 * @param cell
	 * @return true se conseguiu colocar, false caso contrario
	 */
	public boolean coloca_Dragao(Celula cell)
	{
		//Se o dragao nao estiver em cima de um espa�o
		if(maze.getChar(cell) != ' ')
			return false;
		
		//Se o dragao estiver em cima do heroi, ou nas casas adjacentes
		if(cell.equals(hero))
			return false;
		
		if(Math.abs(cell.x - hero.getPosX()) == 1 && cell.y == hero.getPosY()) //Mesmo y; x adjacentes
			return false;
		
		if(cell.x == hero.getPosX() && Math.abs(cell.y - hero.getPosY()) == 1) //Mesmo x; y adjacentes
			return false;
		
		//Se o dragao estiver em cima da espada
		if(cell.equals(sword))
			return false;
		
		//Se o dragao estiver em cima da saida
		if(cell.equals(exit))
			return false;
		
		//Se o dragao estiver em cima de outros dragoes
		for(int i = 0; i < this.dragons.length; i++)
		{
			if(cell.equals(dragons[i]))
				return false;
		}
		
		return true;
	}
	
	/**
	 * @brief converte coordenadas da visitedCells em coordenadas do Maze (amplifica)
	 * @param num
	 * @return Coordenada amplificada
	 */
	public int converter_VisToMaze(int num)
	{
		return (num*2)+1;
	}
	
	/**
	 * @brief converte coordenadas do Maze em coordenadas da visitedCells (reduz)
	 * @param num
	 * @return Coordenada reduzida
	 */
	public int converter_MazeToVis(int num)
	{
		return (num-1)/2;
	}
	
	/**
	 * @brief move a GuideCell em 1 casa de distancia, numa direcao aleatoria
	 * @return true se conseguiu move-la, false caso contrario
	 */
	public boolean moveGuideCell()
	{
	//Gera uma direcao aleatoria
		int direction = generator.nextInt(4);
		
	//Cria copia da guideCell
		Celula celulaInicial = new Celula(guideCell);
		
	//Tenta mover a guideCell nas 4 direcoes
		int i = 0;
		while(i < 4)
		{
			if(moveGuideVisitedCells((direction + i)%4))
				break;
			else
				i++;
		}
	//Se nao conseguir mover em nenhuma das direcoes
		if(i == 4)
			return false;
	
	//Apaga a parede no Maze, abrindo um caminho
		abreCaminhoCelulas(celulaInicial, guideCell);
		
	//Coloca a ultima celula visitada na stack
		lastCells.push(new Celula(guideCell));
		
		return true;
	}
	
	/**
	 * @brief move a GuideCell dentro da visitedCells, sendo com esta fun��o que se decide se
	 * se vai conseguir mover ou se isso � impossivel.
	 * @param direction
	 * @return true se conseguir mover, false se ja nao houver casas para se mover
	 */
	public boolean moveGuideVisitedCells(int direction)
	{
		switch(direction)
		{
		case 0: //Norte
			if(guideCell.y-1 < 0) //Out of bound = nao move a guide cell na VisitedCells
				return false;
			else
			{
				try {
					if(visitedCells.getCell(guideCell.x, guideCell.y-1) == '+') //Celula ja visitada = nao move a guide cell
						return false;
					else
					{
						guideCell.y -= 1;
						escreveGuideVisitedCells();
						return true;
					}
				} catch (Exception e) {
					System.out.println("OUT OF BOUNDS");
					e.printStackTrace();
					return false;
				}
			}
		case 1: //Este
			if(guideCell.x +1 >= visitedCells.getTam())
				return false;
			else
			{
				try {
					if(visitedCells.getCell(guideCell.x+1, guideCell.y) == '+')
						return false;
					else
					{
						guideCell.x += 1;
						escreveGuideVisitedCells();
						return true;
					}
				} catch (Exception e) {
					System.out.println("OUT OF BOUNDS");
					e.printStackTrace();
					return false;
				}
			}
		case 2: //Sul
			if(guideCell.y + 1 >= visitedCells.getTam())
				return false;
			else
			{
				try {
					if(visitedCells.getCell(guideCell.x, guideCell.y+1) == '+')
						return false;
					else
					{
						guideCell.y += 1;
						escreveGuideVisitedCells();
						return true;
					}
				} catch (Exception e) {
					System.out.println("OUT OF BOUNDS");
					e.printStackTrace();
					return false;
				}
			}
		case 3: //Oeste
			if(guideCell.x-1 < 0)
				return false;
			else
			{
				try {
					if(visitedCells.getCell(guideCell.x-1, guideCell.y) == '+')
						return false;
					else
					{
						guideCell.x -= 1;
						escreveGuideVisitedCells();
						return true;
					}
				} catch (Exception e) {
					System.out.println("OUT OF BOUNDS");
					e.printStackTrace();
					return false;
				}
			}
		default:
			return false; //DEVIAMOS INCLUIR UMA EXCECAO
		}
	}
	
	/**
	 * @brief Coloca um + em VisitedCells na posicao da guidecell
	 */
	public void escreveGuideVisitedCells()
	{
		visitedCells.setCell(guideCell.x, guideCell.y); //Coloca um + em VisitedCells na pos da guidecell
		return;
	}
	
	/**
	 * @brief apaga um X entre cell1 e cell2 no Maze que esta a ser gerado
	 * @param cell1
	 * @param cell2
	 */
	public void abreCaminhoCelulas(Celula cell1, Celula cell2)
	{
		//Descobre o x e o y da parede que se quer apagar
		int xmedioMaze = (converter_VisToMaze(cell1.x)+ converter_VisToMaze(cell2.x))/2;
		int ymedioMaze = (converter_VisToMaze(cell1.y)+ converter_VisToMaze(cell2.y))/2;
		
		maze.setChar(' ', xmedioMaze, ymedioMaze);	//Apaga a parede entre as duas celulas
		
		return;
	}
	
	/**
	 * @brief Avan�a a c�lula de constru��o at� parar
	 * @return 
	 */
	public int abreCaminhoAteParar()
	{
		int num_cells = 0;
		while(moveGuideCell())
		{
			num_cells++;
		}
		
		return num_cells;	
	}
	
	/**
	 * @brief Faz pop das c�lulas que foram percorridas, at� encontrar uma
	 * que possa ter um caminho alternativo
	 * @return false - se j� tiver voltado o m�ximo atr�s
	 */
	public boolean voltaAtrasCaminho()
	{
		while(!temNaoVisitadaAdjacente(lastCells.peek()))
		{
			lastCells.pop();
			if(lastCells.isEmpty()) //Quando percorrer todas as op��es
				return false;
		}
		return true;
	}
	
	/**
	 * @brief Verifica se uma c�lula tem alguma c�lula n�o adjacente na grid
	 * das visitedCells
	 * @param cell
	 * @return true - existe uma c�lula n�o visitada
	 */
	public boolean temNaoVisitadaAdjacente(Celula cell)
	{
		int yNorte = cell.y - 1;
		int ySul = cell.y + 1;
		int xEste = cell.x + 1;
		int xOeste = cell.x - 1;
		if(yNorte >= 0) //Se a c�lula a norte estiver in bounds
		{
			try {
				if(visitedCells.getCell(cell.x,yNorte) == '.')
				{
					guideCell.x = cell.x;
					guideCell.y = cell.y;
					return true;
				}
			} catch (Exception e) {
				System.out.println("OUT OF BOUNDS");
				e.printStackTrace();
				return false;
			}
		}
		if(ySul < visitedCells.getTam()) //Se a c�lula a sul estiver in bounds
		{
			try {
				if(visitedCells.getCell(cell.x,ySul) == '.')
				{
					guideCell.x = cell.x;
					guideCell.y = cell.y;
					return true;
				}
			} catch (Exception e) {
				System.out.println("OUT OF BOUNDS");
				e.printStackTrace();
				return false;
			}
		}
		if(xEste < visitedCells.getTam()) //Se a c�lula a este estiver in bounds
		{
			try {
				if(visitedCells.getCell(xEste,cell.y) == '.')
				{
					guideCell.x = cell.x;
					guideCell.y = cell.y;
					return true;
				}
			} catch (Exception e) {
				System.out.println("OUT OF BOUNDS");
				e.printStackTrace();
				return false;
			}
		}
		if(xOeste >= 0)	//Se a c�lula a oeste estiver in bounds
		{
			try {
				if(visitedCells.getCell(xOeste,cell.y) == '.')
				{
					guideCell.x = cell.x;
					guideCell.y = cell.y;
					return true;
				}
			} catch (Exception e) {
				System.out.println("OUT OF BOUNDS");
				e.printStackTrace();
				return false;
			}		
		}
		
		return false;
	}
	
	/**
	 * @brief chama o abreCaminhoAteParar muitas vezes, ate todo o Maze estar desenhado
	 */
	public void abreCaminho()
	{
		do
		{
			abreCaminhoAteParar();
		}while(voltaAtrasCaminho()); //enquanto puder voltar atras no caminho, abre caminho até parar
		
		return;
	}
}