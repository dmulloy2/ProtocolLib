package com.comphenix.protocol.injector.netty.manager;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.netty.channel.InjectionFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

final class InjectionChannelInboundHandler extends ChannelInboundHandlerAdapter {

    private static final ReportType CANNOT_INJECT_CHANNEL = new ReportType("Unable to inject incoming channel %s.");

    private final ErrorReporter errorReporter;
    private final InjectionFactory injectionFactory;

    public InjectionChannelInboundHandler(
            ErrorReporter errorReporter,
            InjectionFactory injectionFactory
    ) {
        this.errorReporter = errorReporter;
        this.injectionFactory = injectionFactory;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // call the minecraft channelActive method first
        ctx.fireChannelActive();

        // the channel is now active, at this point minecraft has eventually prepared everything in the connection
        // of the player so that we can come in and hook as we are after the minecraft handler.
        // We're first checking if the factory is still open, just might be a delay between accepting the connection
        // (which adds this handler to the pipeline) and the actual channelActive call. If the injector is closed at
        // that point we might accidentally trigger class loads which result in exceptions.
        if (!this.injectionFactory.isClosed()) {
            try {
                this.injectionFactory.fromChannel(ctx.channel()).inject();
            } catch (Exception exception) {
                this.errorReporter.reportDetailed(this, Report.newBuilder(CANNOT_INJECT_CHANNEL)
                        .messageParam(ctx.channel())
                        .error(exception)
                        .build());
            }
        }

        // remove this handler from the pipeline now to prevent multiple injections
        ctx.channel().pipeline().remove(this);
    }

    @Override
    public boolean isSharable() {
        // we do it this way to prevent the lookup overhead
        return true;
    }
}
