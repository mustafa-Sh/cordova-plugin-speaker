/********* CordovaPluginIos.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

@interface CordovaPluginIos : CDVPlugin
@end

@implementation CordovaPluginIos

- (void)coolMethodd:(CDVInvokedUrlCommand*)command
{
    NSString* echo = [command.arguments objectAtIndex:0];
    BOOL isJailbroken = [self isDeviceJailbroken];
    
    NSString* responseMessage;
    if (isJailbroken) {
        responseMessage = [NSString stringWithFormat:@"%@ - Jailbroken Device Detected!", echo];
    } else {
        responseMessage = [NSString stringWithFormat:@"%@ - Device is Secure.", echo];
    }
    
    UIAlertController* alert = [UIAlertController alertControllerWithTitle:nil
                                                                   message:responseMessage
                                                            preferredStyle:UIAlertControllerStyleAlert];
    [self.viewController presentViewController:alert animated:YES completion:^{
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 2 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
            [alert dismissViewControllerAnimated:YES completion:nil];
        });
    }];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:responseMessage];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (BOOL)isDeviceJailbroken {
    // Check for the existence of Cydia's path
    if ([[NSFileManager defaultManager] fileExistsAtPath:@"/Applications/Cydia.app"]) {
        return YES;
    }
    // Check for other jailbreak files
    NSArray *jailbreakPaths = @[
        @"/Library/MobileSubstrate/MobileSubstrate.dylib",
        @"/bin/bash",
        @"/usr/sbin/sshd",
        @"/etc/apt"
    ];
    for (NSString *path in jailbreakPaths) {
        if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
            return YES;
        }
    }
    // Check if the app can write outside its sandbox
    NSString *testPath = @"/private/jailbreak_test.txt";
    NSError *error = nil;
    [@"Jailbreak Test" writeToFile:testPath atomically:YES encoding:NSUTF8StringEncoding error:&error];
    if (error == nil) {
        [[NSFileManager defaultManager] removeItemAtPath:testPath error:nil];
        return YES;
    }
    return NO;
}

@end
