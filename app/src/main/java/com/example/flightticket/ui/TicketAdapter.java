package com.example.flightticket.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.flightticket.R;
import com.example.flightticket.network.model.Ticket;
import com.github.ybq.android.spinkit.SpinKitView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.MyViewHolder> {
    Context context;
    TicketsAdapterListener listener;
    List<Ticket> contactList;


    public TicketAdapter(Context context, List<Ticket> contactList, TicketsAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.contactList = contactList;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.airline_name)
        TextView airlineName;

        @BindView(R.id.logo)
        ImageView logo;

        @BindView(R.id.number_of_stops)
        TextView stops;

        @BindView(R.id.number_of_seats)
        TextView seats;

        @BindView(R.id.departure)
        TextView departure;

        @BindView(R.id.arrival)
        TextView arrival;

        @BindView(R.id.duration)
        TextView duration;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.loader)
        SpinKitView loader;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onTicketSelected(contactList.get(getAdapterPosition()));
                }
            });


        }
    }


    @NonNull
    @Override
    public TicketAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketAdapter.MyViewHolder holder, int position) {
        Ticket ticket = contactList.get(position);

        Glide.with(context)
                .load(ticket.getAirline().getLogo())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.logo);
        holder.airlineName.setText(ticket.getAirline().getName());

        holder.departure.setText(ticket.getDeparture() + " Dep");
        holder.arrival.setText(ticket.getArrival() + " Dest");

        holder.duration.setText(ticket.getFlightNumber());
        holder.duration.append(", " + ticket.getDuration());
        holder.stops.setText(ticket.getNumberOfStops() + " Stops");


        if (ticket.getPrice() != null) {
            holder.price.setText("₹" + String.format("%.0f", ticket.getPrice().getPrice()));
            holder.seats.setText(ticket.getPrice().getSeats() + " Seats");
            holder.loader.setVisibility(View.INVISIBLE);
        } else {
            holder.loader.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public interface TicketsAdapterListener {
        void onTicketSelected(Ticket ticket);
    }


}
