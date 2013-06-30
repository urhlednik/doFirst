//
//  ConfigViewController.m
//  SensorTagPS
//
//  Created by Jungsuk Song on 13. 6. 1..
//  Copyright (c) 2013년 Texas Instruments. All rights reserved.
//

#import "ConfigViewController.h"
#import "UIDevice+IdentifierAddition.h"

@interface ConfigViewController ()

@end

@implementation ConfigViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 4;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	const NSInteger DEVICE_ID_LABEL         = 1001;
	const NSInteger DATA_PURPOSE_EDIT       = 1002;
	const NSInteger UPLOAD_SERVER_SWITCH    = 1003;
    const NSInteger SEND_MAIL_BUTTON        = 1004;
    
    
    static NSString *CellIdentifier = @"Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        
        
        
        if (indexPath.row == 0)
        {
            UILabel *title = [[UILabel alloc] initWithFrame:CGRectMake(5, 5, 150, 35)];
            title.text = @"DEVICE_ID";
            [cell addSubview:title];
            
            UILabel *info = [[UILabel alloc] initWithFrame:CGRectMake(130, 5, 200, 35)];
            info.text = [NSString stringWithFormat:@"%@",[[UIDevice currentDevice] uniqueGlobalDeviceIdentifier]];
            [cell addSubview:info];
       }
        else if (indexPath.row == 1)
        {
            UILabel *title = [[UILabel alloc] initWithFrame:CGRectMake(5, 5, 130, 35)];
            title.text = @"DATA_PURPOSE";
            [cell addSubview:title];
            
            UITextField *purpose = [[UITextField alloc] initWithFrame:CGRectMake(130, 5, 200, 35)];
            [cell addSubview:purpose];
        }
        else if (indexPath.row == 2)
        {
            UILabel *title = [[UILabel alloc] initWithFrame:CGRectMake(5, 5, 130, 35)];
            title.text = @"UPLOAD_SERVER";
            [cell addSubview:title];
            
            UISwitch *onOff = [[UISwitch alloc] initWithFrame:CGRectMake(130, 7, 200, 35)];
            [cell addSubview:onOff];
        }
        else if (indexPath.row == 3)
        {            
            UIButton *sendMailButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
            [sendMailButton setTitle:@"SEND MAIL" forState:UIControlStateNormal];
            [sendMailButton setFrame:CGRectMake(5, 5, 250, 35)];
            [cell addSubview:sendMailButton];
        }
        
//        m_configList = [[NSArray alloc] initWithObjects:@"DEVICE_ID"
//                        , @"DATA_PURPOSE"
//                        , @"UPLOAD_SERVER"
//                        , @"SEND_MAIL"
//                        , nil];
        
    }
    else {
//        writerPhotoView = (UIImageView *)[cell viewWithTag:WRITER_PHOTO];
//        writerName = (UILabel *)[cell viewWithTag:WRITER_NAME];
//        messageContent = (UITextView *)[cell viewWithTag:MESSAGE_CONTENT];
//        PhotoView = (UIImageView *)[cell viewWithTag:PHOTO_VIEW];
    }
    
    // Configure the cell...
    
    return cell;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Navigation logic may go here. Create and push another view controller.
    /*
     <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
     [self.navigationController pushViewController:detailViewController animated:YES];
     */
}

@end
