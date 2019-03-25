import com.atlassian.jira.rest.client.api.domain.Issue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class TextForm extends JPanel implements ActionListener  {
  private JTextField[] fields;

  private static final int nElems = 5;
  private static final int fieldWidth = 30;

  private static final String urlLabelText = " JIRA URL: ";
  private static final String userLabelText = " Username: ";
  private static final String passLabelText = " Password: ";
  private static final String fixVersionLabelText = " Fix Version: ";
  private static final String folderLabelText = " Output Folder: ";

  private JTextField urlField;
  private JTextField userField;
  private JTextField passField;
  private JTextField fixField;
  private JTextField folderField;
  private static JButton submit;

  // Create a form with the specified labels and sizes.
  public TextForm() {
    super( new BorderLayout() );
    JPanel labelPanel = new JPanel( new GridLayout( nElems, 1 ) );
    JPanel fieldPanel = new JPanel( new GridLayout( nElems, 1 ) );
    add( labelPanel, BorderLayout.WEST );
    add( fieldPanel, BorderLayout.CENTER );

    // JIRA URL
    urlField = new JTextField(  );
    urlField.setColumns( fieldWidth );
    JLabel urlLabel = new JLabel( urlLabelText, JLabel.RIGHT );
    urlLabel.setLabelFor( urlField );
    labelPanel.add( urlLabel );
    JPanel urlPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    urlPanel.add( urlField );
    fieldPanel.add( urlPanel );

    // Username
    userField = new JTextField(  );
    userField.setColumns( fieldWidth );
    JLabel userLabel = new JLabel( userLabelText, JLabel.RIGHT );
    userLabel.setLabelFor( userField );
    labelPanel.add( userLabel );
    JPanel userPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    userPanel.add( userField );
    fieldPanel.add( userPanel );

    // Password
    passField = new JPasswordField(  );
    passField.setColumns( fieldWidth );
    JLabel passLabel = new JLabel( passLabelText, JLabel.RIGHT );
    passLabel.setLabelFor( passField );
    labelPanel.add( passLabel );
    JPanel passPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    passPanel.add( passField );
    fieldPanel.add( passPanel );

    // Fix Version
    fixField = new JTextField(  );
    fixField.setColumns( fieldWidth );
    JLabel fixLabel = new JLabel( fixVersionLabelText, JLabel.RIGHT );
    fixLabel.setLabelFor( fixField );
    labelPanel.add( fixLabel );
    JPanel fixPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    fixPanel.add( fixField );
    fieldPanel.add( fixPanel );

    // OutputPath Folder
    folderField = new JTextField(  );
    folderField.setColumns( fieldWidth );
    JLabel folderLabel = new JLabel( folderLabelText, JLabel.RIGHT );
    folderLabel.setLabelFor( folderField );
    labelPanel.add( folderLabel );
    JPanel folderPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    folderPanel.add( folderField );
    fieldPanel.add( folderPanel );

    // Generate SP Fixes file button
    submit = new JButton( "Generate File" );
    submit.addActionListener( this );
  }

  public String getUrlField() {
    return urlField.getText();
  }

  public String getUserField() {
    return userField.getText();
  }

  public String getPassField() {
    return passField.getText();
  }

  public String getFixField() {
    return fixField.getText();
  }

  public String getFolderField() {
    return folderField.getText();
  }

  public static void main( String[] args ) {
    final TextForm form = new TextForm();

    // Build Application Window
    JFrame f = new JFrame( "SP Fixes Builder" );
    f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    f.getContentPane().add( form, BorderLayout.NORTH );
    JPanel p = new JPanel();
    p.add( submit );
    f.getContentPane().add( p, BorderLayout.SOUTH );
    f.pack();
    f.setVisible( true );
  }

  /**
   * Generate file button action.
   * @param e
   */
  public void actionPerformed( ActionEvent e ) {
    System.out.println();
    String url = getUrlField();
    String user = getUserField();
    String password = getPassField();
    String fixVersion = getFixField();

    try {
      Iterable<Issue> issues = JRC.getIssuesByFixedVersion( url, user, password, fixVersion );
      buildSPFixesFiles( url, user, password, fixVersion, issues.iterator(), getFolderField() + "SP-Fixes.txt" );
      showSuccessDialog();
    } catch ( Exception e1 ) {
      showErrorDialog();
    }
  }

  /**
   * Write SP Fixes file, for a certain fixVersion, according to JIRA information.
   * @param url
   * @param user
   * @param password
   * @param fixVersion
   * @param issues
   * @throws Exception
   */
  public static void buildSPFixesFiles( String url, String user, String password, String fixVersion, Iterator<Issue> issues, String pathFile ) throws Exception {
    FileWriter fileWriter = new FileWriter( pathFile );
    PrintWriter printWriter = new PrintWriter( fileWriter );
    printWriter.printf( "================================================================================================\n" );
    printWriter.printf( "The JIRA cases listed bellow were fixed on the corresponding service packs\n" );
    printWriter.printf( "================================================================================================\n" );
    printWriter.println( );
    printWriter.println( );
    printWriter.printf( "================================================================================================\n" );
    printWriter.printf( "== %s Fixes ==\n", fixVersion );
    printWriter.println();

    // Sort issue by alphabetic order.
    Iterable<Issue> iterable = () -> issues;
    Stream<Issue> issueStream = StreamSupport.stream( iterable.spliterator(), false );
    List<Issue> list = issueStream.sorted( Comparator.comparing( issue2 -> issue2.getSummary().split( " " )[ 2 ] ) ).collect( Collectors.toList() );

    list.forEach( issue ->
      {
        String issueKey = issue.getSummary().split( " " )[2];

        Issue baseCaseIssue = null;
        try {
          baseCaseIssue = JRC.getIssue( url, user, password, issueKey );
        } catch ( Exception e ) {
          e.printStackTrace();
        }

        printWriter.printf( "%s - %s\n", baseCaseIssue.getKey(), baseCaseIssue.getSummary() );
      }
    );

    printWriter.printf( "================================================================================================" );
    printWriter.close();
  }

  public static void showErrorDialog() {
    JOptionPane optionPane = new JOptionPane( "Something went wrong!", JOptionPane.ERROR_MESSAGE );
    JDialog dialog = optionPane.createDialog( "Error" );
    dialog.setAlwaysOnTop( true ); // to show top of all other application
    dialog.setVisible( true ); // to visible the dialog
  }

  public static void showSuccessDialog() {
    JOptionPane optionPane = new JOptionPane( "SP Fixes file has been created!", JOptionPane.INFORMATION_MESSAGE );
    JDialog dialog = optionPane.createDialog( "Success" );
    dialog.setAlwaysOnTop( true ); // to show top of all other application
    dialog.setVisible( true ); // to visible the dialog
  }
}
